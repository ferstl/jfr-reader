package com.github.ferstl.jfrreader;

import java.util.concurrent.TimeUnit;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

public class InfluxWriter implements AutoCloseable {

  private final InfluxDB influxDb;

  public InfluxWriter() {
    this.influxDb = InfluxDBFactory.connect("http://localhost:8086", "jfr", "jfr");
    this.influxDb.setDatabase("jfr");
  }

  public void writeGcPause(long gcId, String gcName, long startTimeNano, long durationUs) {
    this.influxDb.write(Point.measurement("gc")
        .time(startTimeNano, TimeUnit.NANOSECONDS)
        .addField("gc_name", gcName)
        .addField("gc_id", gcId)
        .addField("pause_time_us", durationUs)
        .tag("application", "jfr-test")
        .tag("event_type", "gc_pause")
        .build());
  }

  @Override
  public void close() {
    this.influxDb.close();
  }
}

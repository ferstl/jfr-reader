package com.github.ferstl.jfrreader;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import static java.util.Objects.requireNonNullElse;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class InfluxWriter implements AutoCloseable {

  private final InfluxDB influxDb;

  public InfluxWriter() {
    this.influxDb = InfluxDBFactory.connect("http://localhost:8086", "jfr", "jfr");
    this.influxDb.setDatabase("jfr");
  }

  public void writeGcPause(long eventStartTimeNs, long gcId, String gcName, long durationUs) {
    this.influxDb.write(Point.measurement("gc")
        .time(eventStartTimeNs, NANOSECONDS)
        .addField("gc_name", gcName)
        .addField("gc_id", gcId)
        .addField("pause_time_us", durationUs)
        .tag("application", "jfr-test")
        .tag("event_type", "gc_pause")
        .build()
    );
  }

  public void writeJvmInfo(long eventStartTimeNs, String jvmName, String jvmVersion, long jvmStartTimeMs, String jvmArgs, String javaArgs) {
    this.influxDb.write(Point.measurement("jvm_info")
        .time(eventStartTimeNs, NANOSECONDS)
        .addField("jvm_name", jvmName)
        .addField("jvm_version", jvmVersion)
        .addField("jvm_start_time_ms", jvmStartTimeMs)
        .addField("jvm_args", requireNonNullElse(jvmArgs, ""))
        .addField("java_args", requireNonNullElse(javaArgs, ""))
        .tag("application", "jfr-test")
        .tag("event_type", "gc_pause")
        .build()
    );
  }

  @Override
  public void close() {
    this.influxDb.close();
  }
}

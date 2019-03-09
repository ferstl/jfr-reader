package com.github.ferstl.jfrreader.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.openjdk.jmc.common.item.IItem;
import com.github.ferstl.jfrreader.EventRecorder;
import com.github.ferstl.jfrreader.extractor.GcPauseEventExtractor;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class GcPauseDataPointCreator implements EventRecorder {

  private final InfluxDB influxDB;
  private final GcPauseEventExtractor eventExtractor;
  private final String pauseType;

  public GcPauseDataPointCreator(InfluxDB influxDB, GcPauseEventExtractor eventExtractor, String pauseType) {
    this.influxDB = influxDB;
    this.eventExtractor = eventExtractor;
    this.pauseType = pauseType;
  }

  @Override
  public void recordEvent(long startTimeEpochNs, IItem item, String applicationName) {
    this.influxDB.write(Point.measurement("gc")
        .time(startTimeEpochNs, NANOSECONDS)
        .addField("gc_name", this.eventExtractor.getGcName(item))
        .addField("gc_id", this.eventExtractor.getGcId(item))
        .addField("pause_time_us", this.eventExtractor.getGcDurationUs(item))
        .tag("pause_type", this.pauseType)
        .tag("application", applicationName)
        .build()
    );
  }
}

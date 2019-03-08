package com.github.ferstl.jfrreader.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.openjdk.jmc.common.item.IItem;
import com.github.ferstl.jfrreader.EventRecorder;
import com.github.ferstl.jfrreader.extractor.JvmInfoEventExtractor;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class JvmInfoDataPointCreator implements EventRecorder {

  private final InfluxDB influxDB;
  private final JvmInfoEventExtractor eventExtractor;

  public JvmInfoDataPointCreator(InfluxDB influxDB, JvmInfoEventExtractor eventExtractor) {
    this.influxDB = influxDB;
    this.eventExtractor = eventExtractor;
  }

  @Override
  public void recordEvent(long startTimeEpochNs, IItem item, String applicationName) {
    this.influxDB.write(Point.measurement("jvm_info")
        .time(startTimeEpochNs, NANOSECONDS)
        .addField("jvm_name", this.eventExtractor.getJvmName(item))
        .addField("jvm_version", this.eventExtractor.getJvmVersion(item))
        .addField("jvm_start_time_ms", this.eventExtractor.getJvmStartTimeMs(item))
        .addField("jvm_args", this.eventExtractor.getJvmArgs(item))
        .addField("java_args", this.eventExtractor.getJavaArgs(item))
        .tag("application", applicationName)
        .build()
    );
  }
}

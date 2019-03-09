package com.github.ferstl.jfrreader.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.openjdk.jmc.common.item.IItem;
import com.github.ferstl.jfrreader.EventRecorder;
import com.github.ferstl.jfrreader.extractor.CpuLoadEventExtractor;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class CpuLoadDataPointCreator implements EventRecorder {

  private final InfluxDB influxDB;
  private final CpuLoadEventExtractor eventExtractor;

  public CpuLoadDataPointCreator(InfluxDB influxDB, CpuLoadEventExtractor eventExtractor) {
    this.influxDB = influxDB;
    this.eventExtractor = eventExtractor;
  }

  @Override
  public void recordEvent(long startTimeEpochNs, IItem item, String applicationName) {
    this.influxDB.write(Point.measurement("cpu_load")
        .time(startTimeEpochNs, NANOSECONDS)
        .addField("jvm_user_percentage", this.eventExtractor.getJvmUserPercent(item))
        .addField("jvm_system_percentage", this.eventExtractor.getJvmSystemPercent(item))
        .addField("machine_total_percentage", this.eventExtractor.getMachineTotalPercent(item))
        .tag("application", applicationName)
        .build()
    );
  }
}

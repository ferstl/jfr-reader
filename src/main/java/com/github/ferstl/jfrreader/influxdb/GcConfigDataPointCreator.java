package com.github.ferstl.jfrreader.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.openjdk.jmc.common.item.IItem;
import com.github.ferstl.jfrreader.EventRecorder;
import com.github.ferstl.jfrreader.extractor.GcConfigEventExtractor;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class GcConfigDataPointCreator implements EventRecorder {

  private final InfluxDB influxDB;
  private final GcConfigEventExtractor eventExtractor;

  public GcConfigDataPointCreator(InfluxDB influxDB, GcConfigEventExtractor eventExtractor) {
    this.influxDB = influxDB;
    this.eventExtractor = eventExtractor;
  }

  @Override
  public void recordEvent(long startTimeEpochNs, IItem item, String applicationName) {
    Boolean explicitGcEnabled = this.eventExtractor.getExplicitGcEnabled(item);
    Boolean explicitGcConcurrent = this.eventExtractor.getExplicitGcConcurrent(item);

    this.influxDB.write(Point.measurement("gc_config")
        .time(startTimeEpochNs, NANOSECONDS)
        .addField("young_collector", this.eventExtractor.getYoungCollector(item))
        .addField("old_collector", this.eventExtractor.getOldCollector(item))
        .addField("nr_of_parallel_threads", this.eventExtractor.getNrOfParallelGcThreads(item))
        .addField("nr_of_concurrent_threads", this.eventExtractor.getNrOfConcurrentGcThreads(item))
        .addField("gc_time_ratio", this.eventExtractor.getGcTimeRatio(item))
        .addField("explicit_gc_enabled", explicitGcEnabled != null ? explicitGcEnabled.toString() : "")
        .addField("explicit_gc_concurrent", explicitGcConcurrent != null ? explicitGcConcurrent.toString() : "")
        .tag("application", applicationName)
        .build()
    );
  }
}

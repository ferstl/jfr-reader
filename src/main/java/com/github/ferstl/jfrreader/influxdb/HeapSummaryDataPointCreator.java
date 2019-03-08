package com.github.ferstl.jfrreader.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.openjdk.jmc.common.item.IItem;
import com.github.ferstl.jfrreader.EventRecorder;
import com.github.ferstl.jfrreader.extractor.HeapSummaryEventExtractor;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class HeapSummaryDataPointCreator implements EventRecorder {

  private final InfluxDB influxDB;
  private final HeapSummaryEventExtractor eventExtractor;

  public HeapSummaryDataPointCreator(InfluxDB influxDB, HeapSummaryEventExtractor eventExtractor) {
    this.influxDB = influxDB;
    this.eventExtractor = eventExtractor;
  }

  @Override
  public void recordEvent(long startTimeEpochNs, IItem item, String applicationName) {
    this.influxDB.write(Point.measurement("gc_heap")
        .time(startTimeEpochNs, NANOSECONDS)
        .addField("gc_id", this.eventExtractor.getGcId(item))
        .addField("when", this.eventExtractor.getWhen(item))
        .addField("committed_heap_bytes", this.eventExtractor.getCommittedHeapBytes(item))
        .addField("reserved_heap_bytes", this.eventExtractor.getReservedHeapBytes(item))
        .addField("used_heap_bytes", this.eventExtractor.getUsedHeapBytes(item))
        .tag("application", applicationName)
        .build()
    );
  }
}

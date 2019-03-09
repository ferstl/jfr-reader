package com.github.ferstl.jfrreader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkTypeIDs;
import com.github.ferstl.jfrreader.extractor.CpuLoadEventExtractor;
import com.github.ferstl.jfrreader.extractor.GcConfigEventExtractor;
import com.github.ferstl.jfrreader.extractor.GcPauseEventExtractor;
import com.github.ferstl.jfrreader.extractor.HeapSummaryEventExtractor;
import com.github.ferstl.jfrreader.extractor.JvmInfoEventExtractor;
import com.github.ferstl.jfrreader.influxdb.CpuLoadDataPointCreator;
import com.github.ferstl.jfrreader.influxdb.GcConfigDataPointCreator;
import com.github.ferstl.jfrreader.influxdb.GcPauseDataPointCreator;
import com.github.ferstl.jfrreader.influxdb.HeapSummaryDataPointCreator;
import com.github.ferstl.jfrreader.influxdb.JvmInfoDataPointCreator;

public class InfluxMain {

  public static void main(String[] args) throws Exception {
    Path recording = Paths.get(args[0]);
    String applicationName = args[1];

    Instant start = Instant.now();
    System.out.println("Start reading Flight Recorder data: (" + start + ")");

    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile());

    Instant startProcessing = Instant.now();
    System.out.println("Flight recorder events read. Took " + Duration.between(start, startProcessing));

    System.out.println("Start processing events (" + startProcessing + ")");
    try (InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "jfr", "jfr")) {
      influxDB.setDatabase("jfr");
      influxDB.enableBatch();
      EventRecorderRegistry registry = createEventRecorderRegistry(influxDB);
      ItemCollectionProcessor itemCollectionProcessor = new ItemCollectionProcessor(applicationName, registry);

      itemCollectionProcessor.processEvents(events);
    }
    System.out.println("Event processing finished. Took " + Duration.between(startProcessing, Instant.now()));
  }

  private static EventRecorderRegistry createEventRecorderRegistry(InfluxDB influxDB) {
    GcPauseEventExtractor gcPauseEventExtractor = new GcPauseEventExtractor();

    return new EventRecorderRegistry(
        Map.of(
            JdkTypeIDs.GC_PAUSE, new GcPauseDataPointCreator(influxDB, gcPauseEventExtractor, "total"),
            JdkTypeIDs.GC_PAUSE_L1, new GcPauseDataPointCreator(influxDB, gcPauseEventExtractor, "l1"),
            JdkTypeIDs.GC_PAUSE_L2, new GcPauseDataPointCreator(influxDB, gcPauseEventExtractor, "l2"),
            JdkTypeIDs.GC_PAUSE_L3, new GcPauseDataPointCreator(influxDB, gcPauseEventExtractor, "l3"),
            JdkTypeIDs.GC_PAUSE_L4, new GcPauseDataPointCreator(influxDB, gcPauseEventExtractor, "l4"),
            JdkTypeIDs.GC_CONF, new GcConfigDataPointCreator(influxDB, new GcConfigEventExtractor()),
            JdkTypeIDs.VM_INFO, new JvmInfoDataPointCreator(influxDB, new JvmInfoEventExtractor()),
            JdkTypeIDs.HEAP_SUMMARY, new HeapSummaryDataPointCreator(influxDB, new HeapSummaryEventExtractor()),
            JdkTypeIDs.CPU_LOAD, new CpuLoadDataPointCreator(influxDB, new CpuLoadEventExtractor())
        )
    );
  }
}

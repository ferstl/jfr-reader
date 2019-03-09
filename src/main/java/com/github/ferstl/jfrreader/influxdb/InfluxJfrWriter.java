package com.github.ferstl.jfrreader.influxdb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.flightrecorder.CouldNotLoadRecordingException;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkTypeIDs;
import com.github.ferstl.jfrreader.ItemCollectionProcessor;
import com.github.ferstl.jfrreader.ItemProcessorRegistry;
import com.github.ferstl.jfrreader.extractor.CpuLoadEventExtractor;
import com.github.ferstl.jfrreader.extractor.GcConfigEventExtractor;
import com.github.ferstl.jfrreader.extractor.GcPauseEventExtractor;
import com.github.ferstl.jfrreader.extractor.HeapSummaryEventExtractor;
import com.github.ferstl.jfrreader.extractor.JvmInfoEventExtractor;

public class InfluxJfrWriter {

  private final IItemCollection events;
  private final String applicationName;

  private InfluxJfrWriter(IItemCollection events, String applicationName) {
    this.events = events;
    this.applicationName = applicationName;
  }

  public static void main(String[] args) {
    Path recording = Paths.get(args[0]);
    String applicationName = args[1];

    InfluxJfrWriter writer = InfluxJfrWriter.fromFile(recording, applicationName);

    writer.writeEvents();
  }

  public static InfluxJfrWriter fromFile(Path flightRecording, String applicationName) {
    try {
      Instant start = Instant.now();
      System.out.println("Start reading Flight Recorder data: (" + start + ")");
      IItemCollection events = JfrLoaderToolkit.loadEvents(flightRecording.toFile());
      System.out.println("Flight recorder events read. Took " + Duration.between(start, Instant.now()));

      return new InfluxJfrWriter(events, applicationName);
    } catch (IOException | CouldNotLoadRecordingException e) {
      throw new IllegalArgumentException("Unable to load flight recording " + flightRecording, e);
    }
  }

  public static InfluxJfrWriter fromData(byte[] data, String applicationName) {
    try {
      Instant start = Instant.now();
      System.out.println("Start reading Flight Recorder data: (" + start + ", " + data.length + " bytes)");
      ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
      IItemCollection events = JfrLoaderToolkit.loadEvents(inputStream);
      System.out.println("Flight recorder events read. Took " + Duration.between(start, Instant.now()));

      return new InfluxJfrWriter(events, applicationName);
    } catch (IOException | CouldNotLoadRecordingException e) {
      throw new IllegalArgumentException("Unable to load flight recording from bytes", e);
    }
  }

  public void writeEvents() {
    Instant startProcessing = Instant.now();
    System.out.println("Start processing events (" + startProcessing + ")");
    try (InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "jfr", "jfr")) {
      influxDB.setDatabase("jfr");
      influxDB.enableBatch();
      ItemProcessorRegistry registry = createEventRecorderRegistry(influxDB);
      ItemCollectionProcessor itemCollectionProcessor = new ItemCollectionProcessor(this.applicationName, registry);

      itemCollectionProcessor.processEvents(this.events);
    }
    System.out.println("Event processing finished. Took " + Duration.between(startProcessing, Instant.now()));
  }

  private static ItemProcessorRegistry createEventRecorderRegistry(InfluxDB influxDB) {
    GcPauseEventExtractor gcPauseEventExtractor = new GcPauseEventExtractor();

    return new ItemProcessorRegistry(
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

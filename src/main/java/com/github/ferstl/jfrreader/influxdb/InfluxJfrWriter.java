package com.github.ferstl.jfrreader.influxdb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
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
  private final ProcessingMetrics processingMetrics;

  private InfluxJfrWriter(IItemCollection events, ProcessingMetrics processingMetrics) {
    this.events = events;
    this.processingMetrics = processingMetrics;
  }

  public static void main(String[] args) {
    Path recording = Paths.get(args[0]);
    String applicationName = args[1];

    InfluxJfrWriter writer = InfluxJfrWriter.fromFile(recording, applicationName);

    writer.writeEvents();
  }

  public static InfluxJfrWriter fromFile(Path flightRecording, String applicationName) {
    try {
      ProcessingMetrics processingMetrics = new ProcessingMetrics();
      processingMetrics.applicationName = applicationName;
      processingMetrics.dataSizeBytes = Files.size(flightRecording);
      processingMetrics.startTime = Instant.now();

      System.out.println("Start reading Flight Recorder data: (" + processingMetrics.startTime + ")");
      IItemCollection events = JfrLoaderToolkit.loadEvents(flightRecording.toFile());
      processingMetrics.parsingDuration = Duration.between(processingMetrics.startTime, Instant.now());
      System.out.println("Flight recorder events read. Took " + processingMetrics.parsingDuration);

      return new InfluxJfrWriter(events, processingMetrics);
    } catch (IOException | CouldNotLoadRecordingException e) {
      throw new IllegalArgumentException("Unable to load flight recording " + flightRecording, e);
    }
  }

  public static InfluxJfrWriter fromData(byte[] data, String applicationName) {
    try {
      ProcessingMetrics processingMetrics = new ProcessingMetrics();
      processingMetrics.applicationName = applicationName;
      processingMetrics.dataSizeBytes = data.length;
      processingMetrics.startTime = Instant.now();

      System.out.println("Start reading Flight Recorder data: (" + processingMetrics.startTime + ", " + data.length + " bytes)");
      ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
      IItemCollection events = JfrLoaderToolkit.loadEvents(inputStream);
      processingMetrics.parsingDuration = Duration.between(processingMetrics.startTime, Instant.now());
      System.out.println("Flight recorder events read. Took " + processingMetrics.parsingDuration);

      return new InfluxJfrWriter(events, processingMetrics);
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
      ItemCollectionProcessor itemCollectionProcessor = new ItemCollectionProcessor(this.processingMetrics.applicationName, registry);

      itemCollectionProcessor.processEvents(this.events);

      this.processingMetrics.endTime = Instant.now();
      this.processingMetrics.processingDuration = Duration.between(startProcessing, this.processingMetrics.endTime);
      writeMetadata(influxDB);
    }
    System.out.println("Event processing finished. Took " + this.processingMetrics.processingDuration);
  }

  private void writeMetadata(InfluxDB influxDB) {
    influxDB.write(Point.measurement("jfr_processing")
        .addField("application_name", this.processingMetrics.applicationName)
        .addField("data_size_bytes", this.processingMetrics.dataSizeBytes)
        .addField("start_time_epochms", this.processingMetrics.startTime.toEpochMilli())
        .addField("end_time_epochms", this.processingMetrics.startTime.toEpochMilli())
        .addField("parsing_duration_ms", this.processingMetrics.parsingDuration.toMillis())
        .addField("processing_duration_ms", this.processingMetrics.processingDuration.toMillis())
        .build()
    );
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

  private static class ProcessingMetrics {

    private String applicationName;
    private long dataSizeBytes;
    private Instant startTime;
    private Duration parsingDuration;
    private Duration processingDuration;
    private Instant endTime;
  }
}

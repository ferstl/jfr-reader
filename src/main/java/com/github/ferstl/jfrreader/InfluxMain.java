package com.github.ferstl.jfrreader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters;
import org.openjdk.jmc.flightrecorder.jdk.JdkTypeIDs;
import com.github.ferstl.jfrreader.extractor.GcConfigEventExtractor;
import com.github.ferstl.jfrreader.extractor.GcPauseEventExtractor;
import com.github.ferstl.jfrreader.extractor.JvmInfoEventExtractor;
import com.github.ferstl.jfrreader.influxdb.GcConfigDataPointCreator;
import com.github.ferstl.jfrreader.influxdb.GcPauseDataPointCreator;
import com.github.ferstl.jfrreader.influxdb.JvmInfoDataPointCreator;

public class InfluxMain {

  public static void main(String[] args) throws Exception {
    Path recording = Paths.get(args[0]);

    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile()).apply(JdkFilters.GC_CONFIG);
    System.out.println("Flight recorder events read.");

    try (InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "jfr", "jfr")) {
      influxDB.setDatabase("jfr");

      EventRecorderRegistry registry = createEventRecorderRegistry(influxDB);
      EventProcessor processor = new EventProcessor(registry);

      processor.processEvents(events);
    }
  }

  private static EventRecorderRegistry createEventRecorderRegistry(InfluxDB influxDB) {
    return new EventRecorderRegistry(
        Map.of(
            JdkTypeIDs.GC_PAUSE, new GcPauseDataPointCreator(influxDB, new GcPauseEventExtractor()),
            JdkTypeIDs.GC_CONF, new GcConfigDataPointCreator(influxDB, new GcConfigEventExtractor()),
            JdkTypeIDs.VM_INFO, new JvmInfoDataPointCreator(influxDB, new JvmInfoEventExtractor())
        )
    );
  }
}

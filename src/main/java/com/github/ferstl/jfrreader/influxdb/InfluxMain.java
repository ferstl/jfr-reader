package com.github.ferstl.jfrreader.influxdb;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import com.github.ferstl.jfrreader.JfrEventProcessor;

public class InfluxMain {

  public static void main(String[] args) {
    Path recording = Paths.get(args[0]);
    JfrEventProcessor reader = JfrEventProcessor.forRecording(recording);
    System.out.println("Loaded recording: " + recording);
    System.exit(0);

    try (InfluxDB influxDb = InfluxDBFactory.connect("http://localhost:8086", "jfr", "jfr")) {
      influxDb.setDatabase("jfr");
      influxDb.enableBatch();

      InfluxJfrEventVisitor visitor = new InfluxJfrEventVisitor(influxDb);
      reader.accept(visitor);

      Map<String, Long> statistics = visitor.getStatistics();
      System.out.println("Statistics for recording");
      for (Entry<String, Long> entry : statistics.entrySet()) {
        System.out.println("Event '" + entry.getKey() + "': " + entry.getValue());
      }
    }
  }

}

package com.github.ferstl.jfrreader.influxdb;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import com.github.ferstl.jfrreader.JfrEventProcessor;

public class InfluxMain {

  public static void main(String[] args) {
    Path recording = Paths.get(args[0]);
    JfrEventProcessor reader = JfrEventProcessor.forRecording(recording);
    System.out.println("Loaded recording: " + recording);

    try (InfluxDB influxDb = InfluxDBFactory.connect("http://localhost:8086", "jfr", "jfr")) {
      influxDb.setDatabase("jfr");
      influxDb.enableBatch();

      InfluxJfrEventVisitor visitor = new InfluxJfrEventVisitor(influxDb);
      reader.accept(visitor);
    }
  }

}

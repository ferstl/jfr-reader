package com.github.ferstl.jfrreader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.github.ferstl.jfrreader.influxdb.InfluxJfrWriter;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import jdk.management.jfr.FlightRecorderMXBean;

public class Main {

  private static final String ONE_MB = "1048576";

  public static void main(String[] args) throws Exception {
    // Use the PID of a running JVM
    String pid = args[0];
    String applicationName = args[1];
    String recordingName = args[2];
    String managementUrl = getLocalManagementUrl(pid);

    try (FlightRecorderConnection connection = FlightRecorderConnection.fromJmxServiceUrl(managementUrl)) {
      FlightRecorderMXBean flightRecorder = connection.getFlightRecorder();
      RecordingInfo originalRecording = connection.getRecordingByName(recordingName);

      Instant nextStartTime = Instant.ofEpochMilli(0);
      while (true) {
        RecordingInfo clonedRecording = cloneRecording(connection, originalRecording);
        byte[] data = readRecording(flightRecorder, clonedRecording, nextStartTime);
        System.out.println("Read " + data.length + " bytes of flight recorder data.");
        InfluxJfrWriter influxJfrWriter = InfluxJfrWriter.fromData(data, applicationName);
        influxJfrWriter.writeEvents();
        nextStartTime = clonedRecording.getStopTime();

        System.out.println("Waiting for next iteration");
        TimeUnit.SECONDS.sleep(60);
      }
    }
  }

  private static String getLocalManagementUrl(String pid) throws AttachNotSupportedException, IOException {
    VirtualMachine vm = VirtualMachine.attach(pid);
    String jmxServiceUrl = vm.startLocalManagementAgent();
    vm.detach();

    return jmxServiceUrl;
  }

  private static RecordingInfo cloneRecording(FlightRecorderConnection connection, RecordingInfo originalRecording) {
    FlightRecorderMXBean flightRecorder = connection.getFlightRecorder();
    long cloneId = flightRecorder.cloneRecording(originalRecording.getId(), true);
    flightRecorder.setRecordingOptions(cloneId, Map.of("name", "jfr stream clone of " + originalRecording.getName()));

    return connection.getRecordingById(cloneId);
  }

  private static byte[] readRecording(FlightRecorderMXBean flightRecorder, RecordingInfo recording, Instant startTime) throws IOException {
    long recordingId = recording.getId();
    long streamId = flightRecorder.openStream(recordingId, Map.of("startTime", startTime.toString(), "blockSize", ONE_MB));
    ByteBuffer buffer = ByteBuffer.allocate((int) recording.getSize());
    for (byte[] bytes = flightRecorder.readStream(streamId); bytes != null; bytes = flightRecorder.readStream(streamId)) {
      buffer.put(bytes);
    }

    flightRecorder.closeRecording(recordingId);
    buffer.flip();

    System.out.println("Read " + buffer.limit() + " bytes");
    byte[] result = new byte[buffer.limit()];
    System.arraycopy(buffer.array(), 0, result, 0, result.length);
    return result;
  }

}

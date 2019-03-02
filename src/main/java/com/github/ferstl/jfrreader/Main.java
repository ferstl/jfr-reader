package com.github.ferstl.jfrreader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.flightrecorder.CouldNotLoadRecordingException;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import jdk.management.jfr.FlightRecorderMXBean;

public class Main {

  private static final String ONE_MB = "1048576";

  public static void main(String[] args) throws Exception {
    // Use the PID of a running JVM
    String pid = args[0];
    String recordingName = args[1];
    String managementUrl = getLocalManagementUrl(pid);

    try (FlightRecorderConnection connection = FlightRecorderConnection.fromJmxServiceUrl(managementUrl)) {
      FlightRecorderMXBean flightRecorder = connection.getFlightRecorder();
      RecordingInfo originalRecording = connection.getRecordingByName(recordingName);

      Instant nextStartTime = Instant.ofEpochMilli(0);
      for (int i = 0; i < 1000; i++) {
        System.out.println("Start iteration " + i);
        RecordingInfo clonedRecording = cloneRecording(connection, originalRecording);
        byte[] data = readRecording(flightRecorder, clonedRecording, nextStartTime);
        parseRecording(data);
        nextStartTime = clonedRecording.getStopTime();
      }
    }
  }

  private static void parseRecording(byte[] data) throws IOException, CouldNotLoadRecordingException {
    IItemCollection events = JfrLoaderToolkit.loadEvents(new ByteArrayInputStream(data));
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

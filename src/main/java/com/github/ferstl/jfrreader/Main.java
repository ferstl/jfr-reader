package com.github.ferstl.jfrreader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.flightrecorder.CouldNotLoadRecordingException;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import jdk.management.jfr.FlightRecorderMXBean;

public class Main {

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
        long cloneId = cloneRecording(flightRecorder, originalRecording);
        RecordingInfo clonedRecording = connection.getRecordingById(cloneId);
        byte[] data = readRecording(flightRecorder, cloneId, nextStartTime);
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

  private static long cloneRecording(FlightRecorderMXBean flightRecorder, RecordingInfo recording) {
    long clonedRecording = flightRecorder.cloneRecording(recording.getId(), true);
    flightRecorder.setRecordingOptions(clonedRecording, Map.of("name", "jfr stream clone of " + recording.getName()));

    return clonedRecording;
  }

  private static byte[] readRecording(FlightRecorderMXBean flightRecorder, long cloneId, Instant startTime) throws IOException {
    long streamId = flightRecorder.openStream(cloneId, Map.of("startTime", startTime.toString()));
    byte[] data = new byte[0];
    for (byte[] bytes = flightRecorder.readStream(streamId); bytes != null; bytes = flightRecorder.readStream(streamId)) {
      data = concat(data, bytes);
    }

    flightRecorder.closeRecording(cloneId);

    System.out.println("Read " + data.length + " Bytes");
    return data;
  }

  private static byte[] concat(byte[] a, byte[] b) {
    byte[] concatenated = new byte[a.length + b.length];
    System.arraycopy(a, 0, concatenated, 0, a.length);
    System.arraycopy(b, 0, concatenated, a.length, b.length);

    return concatenated;
  }

}

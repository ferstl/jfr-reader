package com.github.ferstl.jfrreader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import org.openjdk.jmc.common.IDisplayable;
import org.openjdk.jmc.common.item.Aggregators;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.ItemFilters;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkTypeIDs;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import jdk.management.jfr.FlightRecorderMXBean;

public class Main {

  public static void main(String[] args) throws Exception {
    // Use the PID of a running JVM
    String pid = args[0];
    String recording = args[1];
    String managementUrl = getLocalManagementUrl(pid);

    try (FlightRecorderConnection connection = FlightRecorderConnection.fromJmxServiceUrl(managementUrl)) {
      FlightRecorderMXBean flightRecorder = connection.getFlightRecorder();
      RecordingInfo recordingInfo = connection.getRecordings().stream()
          .filter(info -> recording.equals(info.getName()))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Recording '" + recording + "' not found in JVM '" + pid + "'"));

      long clonedRecording = cloneRecording(flightRecorder, recordingInfo);

      long streamId = flightRecorder.openStream(clonedRecording, Map.of());
      byte[] data = new byte[0];
      for (byte[] bytes = flightRecorder.readStream(streamId); bytes != null; bytes = flightRecorder.readStream(streamId)) {
        data = concat(data, bytes);
      }

      flightRecorder.closeRecording(clonedRecording);

      IItemCollection events = JfrLoaderToolkit.loadEvents(new ByteArrayInputStream(data));
      IQuantity aggregate = events.apply(ItemFilters.type(JdkTypeIDs.MONITOR_ENTER))
          .getAggregate(Aggregators.stddev(JfrAttributes.DURATION));

      System.out.println("The standard deviation for the Java monitor enter events was "
          + aggregate.displayUsing(IDisplayable.AUTO));
    }
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

  private static byte[] concat(byte[] a, byte[] b) {
    byte[] concatenated = new byte[a.length + b.length];
    System.arraycopy(a, 0, concatenated, 0, a.length);
    System.arraycopy(b, 0, concatenated, a.length, b.length);

    return concatenated;
  }

}

package com.github.ferstl.jfrreader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
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
    FlightRecorderMXBean flightRecorder = getFlightRecorder(pid);

    int recordingId = 1;
    long clonedRecording = cloneRecording(flightRecorder, recordingId);

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

  private static FlightRecorderMXBean getFlightRecorder(String pid) throws AttachNotSupportedException, IOException, MalformedObjectNameException {
    VirtualMachine vm = VirtualMachine.attach(pid);

    String jmxServiceUrl = vm.startLocalManagementAgent();
    JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(jmxServiceUrl));
    MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();

    ObjectName objectName = new ObjectName("jdk.management.jfr:type=FlightRecorder");
    return JMX.newMXBeanProxy(mBeanServerConnection, objectName, FlightRecorderMXBean.class);
  }

  private static long cloneRecording(FlightRecorderMXBean flightRecorder, int recordingId) {
    long clonedRecording = flightRecorder.cloneRecording(recordingId, true);

    Map<String, String> recordingOptions = flightRecorder.getRecordingOptions(clonedRecording);
    String name = recordingOptions.getOrDefault("name", "unnamed recording");
    name = name.replace("Clone of ", "");
    flightRecorder.setRecordingOptions(clonedRecording, Map.of("name", "jfr stream clone of " + name));

    return clonedRecording;
  }

  private static byte[] concat(byte[] a, byte[] b) {
    byte[] concatenated = new byte[a.length + b.length];
    System.arraycopy(a, 0, concatenated, 0, a.length);
    System.arraycopy(b, 0, concatenated, a.length, b.length);

    return concatenated;
  }

}

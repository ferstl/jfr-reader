package com.github.ferstl.jfrreader;

import java.io.IOException;
import java.util.Map;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import jdk.management.jfr.FlightRecorderMXBean;

public class Main {

  public static void main(String[] args) throws Exception {
    // Use the PID of a running JVM
    String pid = args[0];
    FlightRecorderMXBean flightRecorder = getFlightRecorder(pid);

    long clonedRecording = flightRecorder.cloneRecording(1, true);
    long streamId = flightRecorder.openStream(clonedRecording, Map.of());

    for (byte[] bytes = flightRecorder.readStream(streamId); bytes != null; bytes = flightRecorder.readStream(streamId)) {
      System.out.println("Read " + bytes.length + " bytes");
    }

    flightRecorder.closeRecording(clonedRecording);

  }

  private static FlightRecorderMXBean getFlightRecorder(String pid) throws AttachNotSupportedException, IOException, MalformedObjectNameException {
    VirtualMachine vm = VirtualMachine.attach(pid);

    String jmxServiceUrl = vm.startLocalManagementAgent();
    JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(jmxServiceUrl));
    MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();

    ObjectName objectName = new ObjectName("jdk.management.jfr:type=FlightRecorder");
    return JMX.newMXBeanProxy(mBeanServerConnection, objectName, FlightRecorderMXBean.class);
  }

}

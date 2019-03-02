package com.github.ferstl.jfrreader;

import java.io.IOException;
import java.io.UncheckedIOException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import jdk.management.jfr.FlightRecorderMXBean;

public class FlightRecorderConnection implements AutoCloseable {

  private static final ObjectName JFR_OBJECT_NAME;

  static {
    try {
      JFR_OBJECT_NAME = new ObjectName("jdk.management.jfr:type=FlightRecorder");
    } catch (MalformedObjectNameException e) {
      throw new RuntimeException("Should never happen", e);
    }
  }

  private final JMXConnector jmxConnector;
  private final FlightRecorderMXBean flightRecorder;

  private FlightRecorderConnection(JMXConnector jmxConnector, FlightRecorderMXBean flightRecorder) {
    this.jmxConnector = jmxConnector;
    this.flightRecorder = flightRecorder;
  }

  public static FlightRecorderConnection fromJmxServiceUrl(String jmxServiceUrl) {
    try {
      JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(jmxServiceUrl));
      MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();

      FlightRecorderMXBean flightRecorder = JMX.newMXBeanProxy(mBeanServerConnection, JFR_OBJECT_NAME, FlightRecorderMXBean.class);

      return new FlightRecorderConnection(jmxConnector, flightRecorder);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public FlightRecorderMXBean getFlightRecorder() {
    return this.flightRecorder;
  }

  @Override
  public void close() throws Exception {
    this.jmxConnector.close();
  }
}

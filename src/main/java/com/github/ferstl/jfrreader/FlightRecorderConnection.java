package com.github.ferstl.jfrreader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
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
  private final MBeanServerConnection mBeanServerConnection;
  private final FlightRecorderMXBean flightRecorder;

  private FlightRecorderConnection(JMXConnector jmxConnector, MBeanServerConnection mBeanServerConnection, FlightRecorderMXBean flightRecorder) {
    this.jmxConnector = jmxConnector;
    this.mBeanServerConnection = mBeanServerConnection;
    this.flightRecorder = flightRecorder;
  }

  public static FlightRecorderConnection fromJmxServiceUrl(String jmxServiceUrl) {
    try {
      JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(jmxServiceUrl));
      MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();

      FlightRecorderMXBean flightRecorder = JMX.newMXBeanProxy(mBeanServerConnection, JFR_OBJECT_NAME, FlightRecorderMXBean.class);

      return new FlightRecorderConnection(jmxConnector, mBeanServerConnection, flightRecorder);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public FlightRecorderMXBean getFlightRecorder() {
    return this.flightRecorder;
  }

  // Workaround for https://bugs.openjdk.java.net/browse/JDK-8219904
  public List<RecordingInfo> getRecordings() {
    CompositeData[] recordings;
    try {
      recordings = (CompositeData[]) this.mBeanServerConnection.getAttribute(JFR_OBJECT_NAME, "Recordings");
    } catch (Exception e) {
      throw new IllegalStateException("Unable to read recordings", e);
    }

    return Arrays.stream(recordings)
        .map(RecordingInfo::new)
        .collect(Collectors.toList());
  }

  public RecordingInfo getRecordingByName(String name) {
    return getRecordings().stream()
        .filter(recordingInfo -> name.equals(recordingInfo.getName()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Recording '" + name + "' not found."));
  }

  public RecordingInfo getRecordingById(long id) {
    return getRecordings().stream()
        .filter(recordingInfo -> recordingInfo.getId() == id)
        .findAny()
        .orElseThrow(() -> new IllegalStateException("Recording '" + id + "' not found."));
  }

  @Override
  public void close() throws Exception {
    this.jmxConnector.close();
  }

}

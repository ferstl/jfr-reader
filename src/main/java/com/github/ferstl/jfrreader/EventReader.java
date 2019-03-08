package com.github.ferstl.jfrreader;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IType;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters;
import org.openjdk.jmc.flightrecorder.jdk.JdkTypeIDs;

public class EventReader {

  public static void main(String[] args) throws Exception {
    Path recording = Paths.get(args[0]);

    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile()).apply(JdkFilters.VM_INFO);
    System.out.println("Flight recorder events read.");

    try (InfluxWriter influxWriter = new InfluxWriter()) {
      for (IItemIterable eventTypeEntry : events) {
        IType<IItem> type = eventTypeEntry.getType();
        if (JdkTypeIDs.GC_PAUSE.equals(type.getIdentifier())) {
          for (IItem item : eventTypeEntry) {
            recordGcPause(item, type, influxWriter);
          }
        } else if (JdkTypeIDs.VM_INFO.equals(type.getIdentifier())) {
          for (IItem item : eventTypeEntry) {
            recordVmInfo(item, type, influxWriter);
          }
        }

      }
    }
    System.out.println(events);
  }

  private static void recordVmInfo(IItem item, IType<IItem> type, InfluxWriter influxWriter) {
    long eventStartTimeNs = JfrAttributes.START_TIME.getAccessor(type).getMember(item).clampedLongValueIn(UnitLookup.EPOCH_NS);
    String jvmName = JdkAttributes.JVM_NAME.getAccessor(type).getMember(item);
    String jvmVersion = JdkAttributes.JVM_VERSION.getAccessor(type).getMember(item);
    long jvmStartTimeMs = JdkAttributes.JVM_START_TIME.getAccessor(type).getMember(item).clampedLongValueIn(UnitLookup.EPOCH_MS);
    String jvmArgs = JdkAttributes.JVM_ARGUMENTS.getAccessor(type).getMember(item);
    String javaArgs = JdkAttributes.JAVA_ARGUMENTS.getAccessor(type).getMember(item);

    influxWriter.writeJvmInfo(eventStartTimeNs, jvmName, jvmVersion, jvmStartTimeMs, jvmArgs, javaArgs);
  }

  private static void recordGcPause(IItem item, IType<IItem> type, InfluxWriter influxWriter) {
    long gcId = JdkAttributes.GC_ID.getAccessor(type).getMember(item).longValue();
    String gcName = JdkAttributes.GC_NAME.getAccessor(type).getMember(item);
    long startTimeNano = JfrAttributes.START_TIME.getAccessor(type).getMember(item).clampedLongValueIn(UnitLookup.EPOCH_NS);
    long durationUs = JfrAttributes.DURATION.getAccessor(type).getMember(item).clampedLongValueIn(UnitLookup.MICROSECOND);

    influxWriter.writeGcPause(startTimeNano, gcId, gcName, durationUs);
  }
}

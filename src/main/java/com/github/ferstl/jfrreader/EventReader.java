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

public class EventReader {

  public static void main(String[] args) throws Exception {
    Path recording = Paths.get(args[0]);

    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile()).apply(JdkFilters.GC_PAUSE);
    System.out.println("Flight recorder events read.");

    try (InfluxWriter influxWriter = new InfluxWriter()) {
      for (IItemIterable eventTypeEntry : events) {
        IType<IItem> type = eventTypeEntry.getType();

        for (IItem item : eventTypeEntry) {
          long gcId = JdkAttributes.GC_ID.getAccessor(type).getMember(item).longValue();
          String gcName = JdkAttributes.GC_NAME.getAccessor(type).getMember(item);
          long startTimeNano = JfrAttributes.START_TIME.getAccessor(type).getMember(item).clampedLongValueIn(UnitLookup.EPOCH_NS);
          long durationUs = JfrAttributes.DURATION.getAccessor(type).getMember(item).clampedLongValueIn(UnitLookup.MICROSECOND);

          influxWriter.writeGcPause(gcId, gcName, startTimeNano, durationUs);
        }
      }
    }
    System.out.println(events);
  }
}

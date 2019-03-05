package com.github.ferstl.jfrreader;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.openjdk.jmc.common.IDisplayable;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IType;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters;

public class EventReader {

  public static void main(String[] args) throws Exception {
    Path recording = Paths.get(args[0]);

    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile()).apply(JdkFilters.GC_PAUSE);

    for (IItemIterable eventTypeEntry : events) {
      IType<IItem> type = eventTypeEntry.getType();

      for (IItem item : eventTypeEntry) {
        IQuantity member = JfrAttributes.DURATION.getAccessor(type).getMember(item);
        System.out.println(member.clampedLongValueIn(UnitLookup.NANOSECOND) + " -> " + member.displayUsing(IDisplayable.AUTO));
      }
    }

    System.out.println(events);
  }

}

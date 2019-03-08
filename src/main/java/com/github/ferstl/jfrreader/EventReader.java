package com.github.ferstl.jfrreader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
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

    InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "jfr", "jfr");
    String dbName = "jfr";
    influxDB.setDatabase(dbName);

    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile()).apply(JdkFilters.GC_PAUSE);

    for (IItemIterable eventTypeEntry : events) {
      IType<IItem> type = eventTypeEntry.getType();

      for (IItem item : eventTypeEntry) {
        long gcId = JdkAttributes.GC_ID.getAccessor(type).getMember(item).longValue();
        String gcName = JdkAttributes.GC_NAME.getAccessor(type).getMember(item);
        long startTimeNano = JfrAttributes.START_TIME.getAccessor(type).getMember(item).clampedLongValueIn(UnitLookup.EPOCH_NS);
        long durationUs = JfrAttributes.DURATION.getAccessor(type).getMember(item).clampedLongValueIn(UnitLookup.MICROSECOND);

        write(influxDB, gcId, gcName, startTimeNano, durationUs);
      }
    }

    influxDB.close();
    System.out.println(events);
  }

  private static void write(InfluxDB influxDB, long gcId, String gcName, long startTimeNano, long durationUs) {
    influxDB.write(Point.measurement("gc")
        .time(startTimeNano, TimeUnit.NANOSECONDS)
        .addField("gc_name", gcName)
        .addField("gc_id", gcId)
        .addField("pause_time_us", durationUs)
        .tag("application", "jfr-test")
        .tag("event_type", "gc_pause")
        .build());
  }

}

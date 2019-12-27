package com.github.ferstl.jfrreader.experiments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.openjdk.jmc.common.item.IAccessorKey;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IMemberAccessor;
import org.openjdk.jmc.common.item.IType;
import org.openjdk.jmc.common.unit.ContentType;
import org.openjdk.jmc.common.unit.ITypedQuantity;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;

public class JfrAttributeAnalyzer {

  public static void main(String[] args) throws Exception {

    Path recording = Paths.get(args[0]);
    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile());
    List<JfrEventData> eventDataList = new ArrayList<>();

    Map<String, Set<String>> memberClassesByContentType = new TreeMap<>();

    System.out.println("Events loaded\n");
    for (IItemIterable eventTypeEntry : events) {
      IType<IItem> eventType = eventTypeEntry.getType();
      //      System.out.println("Event: " + eventType.getIdentifier());

      for (IItem eventItem : eventTypeEntry) {
        Set<IAccessorKey<?>> accessorKeys = eventType.getAccessorKeys().keySet();

        for (IAccessorKey<?> accessorKey : accessorKeys) {
          IMemberAccessor<?, IItem> accessor = eventType.getAccessor(accessorKey);
          Object member = accessor.getMember(eventItem);

          String contentType = accessorKey.getContentType().toString();
          memberClassesByContentType.computeIfAbsent(contentType, key -> new LinkedHashSet<>());
          String memberInfo = member != null ? member.getClass().toString() : "n/a";
          if (member instanceof ITypedQuantity) {
            memberInfo += " " + ((ITypedQuantity) member).getUnit();
          }
          memberClassesByContentType.get(contentType).add(memberInfo);

          //JfrEventData eventData = createEventData(eventType.getIdentifier(), accessorKey, member);
          //eventDataList.add(eventData);
          //          if (member != null) {
          //            System.out.println("Identifier: " + accessorKey.getIdentifier() + ", ContentType: " + accessorKey.getContentType() + ", Member class: " + member.getClass() + ", Member value: " + member);
          //          } else {
          //            System.out.println("null: " + accessorKey.getIdentifier() + ", " + accessorKey.getContentType());
          //          }
        }

        eventType.getAccessorKeys();
      }
    }

    memberClassesByContentType.entrySet()
        .forEach(entry -> {
          System.out.println("Content type: " + entry.getKey());
          entry.getValue().stream().forEach(System.out::println);
          System.out.println("-------------------------------");
        });

    //    Map<String, List<JfrEventData>> eventDataByContentType = eventDataList.stream()
    //        .collect(groupingBy(eventData -> eventData.contentType));
    //
    //    eventDataByContentType.forEach((key, value) -> {
    //      System.out.println("Content type: " + key);
    //      value.stream().map(eventData -> eventData.memberClass).distinct().forEach(System.out::println);
    //      System.out.println("-------------------------------\n");
    //    });

  }

  private static JfrEventData createEventData(String eventName, IAccessorKey<?> accessorKey, Object member) {
    String identifier = accessorKey.getIdentifier();
    ContentType<?> contentType = accessorKey.getContentType();
    String memberClass = "n/a";
    String memberValue = "n/a";
    String unit = "n/a";

    if (member != null) {
      memberClass = member.getClass().getName();
      memberValue = member.toString();
      if (member instanceof ITypedQuantity) {
        unit = ((ITypedQuantity) member).getUnit().toString();
      }
    }

    return new JfrEventData(eventName, identifier, contentType.toString(), memberClass, memberValue, unit);
  }


  static final class JfrEventData {

    final String eventName;
    final String attributeName;
    final String contentType;
    final String memberClass;
    final String memberValue;
    final String unit;

    JfrEventData(String eventName, String attributeName, String contentType, String memberClass, String memberValue, String unit) {
      this.eventName = eventName;
      this.attributeName = attributeName;
      this.contentType = contentType;
      this.memberClass = memberClass;
      this.memberValue = memberValue;
      this.unit = unit;
    }
  }
}

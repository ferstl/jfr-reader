package com.github.ferstl.jfrreader.experiments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.openjdk.jmc.common.IDescribable;
import org.openjdk.jmc.common.IMCClassLoader;
import org.openjdk.jmc.common.IMCFrame;
import org.openjdk.jmc.common.IMCMethod;
import org.openjdk.jmc.common.IMCModule;
import org.openjdk.jmc.common.IMCOldObject;
import org.openjdk.jmc.common.IMCOldObjectArray;
import org.openjdk.jmc.common.IMCOldObjectField;
import org.openjdk.jmc.common.IMCOldObjectGcRoot;
import org.openjdk.jmc.common.IMCPackage;
import org.openjdk.jmc.common.IMCStackTrace;
import org.openjdk.jmc.common.IMCThread;
import org.openjdk.jmc.common.IMCThreadGroup;
import org.openjdk.jmc.common.IMCType;
import org.openjdk.jmc.common.item.IAccessorKey;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IMemberAccessor;
import org.openjdk.jmc.common.item.IType;
import org.openjdk.jmc.common.unit.ContentType;
import org.openjdk.jmc.common.unit.ITypedQuantity;
import org.openjdk.jmc.common.util.LabeledIdentifier;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;

public class JfrGenericReader {

  public static void main(String[] args) throws Exception {
    Path recording = Paths.get(args[0]);
    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile());
    System.out.println("Loaded recording: " + recording);

    // analysis...
    Map<String, ContentType<?>> accessorIdentifiers = new TreeMap<>();
    HashSet<Class<?>> types = new HashSet<>();


    for (IItemIterable eventTypeEntry : events) {
      IType<IItem> eventType = eventTypeEntry.getType();

      String eventIdentifier = eventType.getIdentifier();
      Map<IAccessorKey<?>, ? extends IDescribable> accessorKeys = eventType.getAccessorKeys();

      for (IItem eventItem : eventTypeEntry) {
        for (IAccessorKey<?> accessorKey : accessorKeys.keySet()) {
          accessorIdentifiers.put(accessorKey.getIdentifier(), accessorKey.getContentType());

          IMemberAccessor<?, IItem> accessor = eventType.getAccessor(accessorKey);
          Object value = accessor.getMember(eventItem);

          types.add(value != null ? value.getClass() : null);
          processMember(eventType, accessorKey, value);
        }
      }

    }

    System.out.println();
    System.out.println("All accessors");
    for (Entry<String, ContentType<?>> entry : accessorIdentifiers.entrySet()) {
      String identifier = entry.getKey();
      ContentType<?> contentType = entry.getValue();
      System.out.println(identifier + ": " + contentType);
    }

    System.out.println();
    System.out.println("All types");
    for (Class<?> type : types) {
      System.out.println(type);
    }
  }

  private static void processMember(IType<IItem> eventType, IAccessorKey<?> accessorKey, Object value) {
    if (value == null) {
      return;
    }

    if (value instanceof String) {

    } else if (value instanceof Boolean) {


    } else if (value instanceof ITypedQuantity) {
      if ("org.openjdk.jmc.common.unit.ScalarQuantity$LongStored".equals(value.getClass().getName())) {

      } else if ("org.openjdk.jmc.common.unit.ScalarQuantity$LongStored".equals(value.getClass().getName())) {

      }
    } else if (value instanceof LabeledIdentifier) {

    } else if (value instanceof IMCClassLoader) {

    } else if (value instanceof IMCFrame) {

    } else if (value instanceof IMCMethod) {

    } else if (value instanceof IMCModule) {

    } else if (value instanceof IMCOldObject) {

    } else if (value instanceof IMCOldObjectArray) {

    } else if (value instanceof IMCOldObjectField) {

    } else if (value instanceof IMCOldObjectGcRoot) {

    } else if (value instanceof IMCPackage) {

    } else if (value instanceof IMCStackTrace) {

    } else if (value instanceof IMCThread) {

    } else if (value instanceof IMCThreadGroup) {

    } else if (value instanceof IMCType) {

    } else {
      System.out.println("+".repeat(20) + " Unsupported Type:" + value.getClass() + "+".repeat(20));
    }
  }
}

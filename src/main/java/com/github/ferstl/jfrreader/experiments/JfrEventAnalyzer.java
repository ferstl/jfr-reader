package com.github.ferstl.jfrreader.experiments;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.openjdk.jmc.common.IDescribable;
import org.openjdk.jmc.common.item.Attribute;
import org.openjdk.jmc.common.item.IAccessorKey;
import org.openjdk.jmc.common.item.IAttribute;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IType;
import org.openjdk.jmc.common.unit.ContentType;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import org.openjdk.jmc.flightrecorder.jdk.JdkTypeIDs;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class JfrEventAnalyzer {

  public static void main(String[] args) throws Exception {
    Map<String, JfrEventType> eventTypes = readEventTypes();
    Map<String, List<JfrAttribute>> attributes = readAttributes();

    System.err.println("Event Types: ");
    for (String eventType : eventTypes.keySet()) {
      System.err.println(eventType);
    }

    System.err.println();
    System.err.println("Attributes:");
    for (Entry<String, List<JfrAttribute>> entry : attributes.entrySet()) {
      IAttribute<?> a = entry.getValue().get(0).getAttribute();
      IAttribute<?> attr = Attribute.attr(a.getIdentifier(), a.getName(), a.getContentType());
      System.err.println(attr.getContentType());
      System.err.println("----------" + attr);
    }

    Path recording = Paths.get(args[0]);
    IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile());

    Set<String> foundEvents = new TreeSet<>();
    Set<String> foundAttributes = new TreeSet<>();
    Set<String> foundContentTypes = new TreeSet<>();
    Set<String> contentTypeClasses = new TreeSet<>();

    for (IItemIterable eventTypeEntry : events) {
      IType<IItem> type = eventTypeEntry.getType();
      JfrEventType jfrEventType = eventTypes.get(type.getIdentifier());
      if (jfrEventType != null) {
        foundEvents.add(jfrEventType.getIdentifier());
      }

      for (Entry<IAccessorKey<?>, ? extends IDescribable> accessorKeyEntry : type.getAccessorKeys().entrySet()) {
        IAccessorKey<?> accessorKey = accessorKeyEntry.getKey();
        ContentType<?> contentType = UnitLookup.getContentType(accessorKey.getContentType().getIdentifier());
        foundAttributes.add(accessorKey.getIdentifier() + ", " + accessorKey.getContentType());
        foundContentTypes.add(accessorKey.getContentType().getIdentifier());

        contentTypeClasses.add("Content Type: " + accessorKey.getContentType().getIdentifier()
            + "Content Type Unit: "
            + contentType.getClass().getName()
            + " / " + "Content Type Accessor: " + accessorKey.getContentType().getClass().getName());
        //if ("index".equals(accessorKey.getContentType().getIdentifier())) {
        for (IItem iItem : eventTypeEntry) {
          iItem.toString();
        }
        //}
      }

    }

    System.err.println("\nFound Attributes");
    for (String foundAttribute : foundAttributes) {
      System.err.println(foundAttribute + " -> " + attributes.containsKey(foundAttribute));
    }

    System.err.println("\nFound Content Types");
    for (String foundContentType : foundContentTypes) {
      System.err.println(foundContentType);
    }

    System.err.println("\nFound Content Type Classes");
    for (String contentTypeClass : contentTypeClasses) {
      System.err.println(contentTypeClass);
    }

    TreeSet<String> notFoundEvents = new TreeSet<>(eventTypes.keySet());
    notFoundEvents.removeAll(foundEvents);

    System.err.println("\nNot found events:");
    notFoundEvents.forEach(System.err::println);

  }

  private static Map<String, JfrEventType> readEventTypes() {
    return Stream.of(JdkTypeIDs.class.getFields())
        .filter(field -> isPublicConstant(field) && field.getType() == String.class)
        .map(field -> {
          try {
            return new JfrEventType(field.getName(), (String) field.get(null));
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(toMap(JfrEventType::getIdentifier, identity()));
  }

  private static Map<String, List<JfrAttribute>> readAttributes() {
    List<JfrAttribute> jfrAttributes = Stream.of(JdkAttributes.class.getFields())
        .filter(field -> isPublicConstant(field) && field.getType() == IAttribute.class)
        .map(field -> {
          try {
            return new JfrAttribute(field.getName(), (IAttribute<?>) field.get(null));
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(toList());

    Map<String, List<JfrAttribute>> attributeMap = new HashMap<>();
    for (JfrAttribute jfrAttribute : jfrAttributes) {
      List<JfrAttribute> attributes = attributeMap.computeIfAbsent(jfrAttribute.getIdentifier(), key -> new ArrayList<>());
      attributes.add(jfrAttribute);
    }

    return attributeMap;
  }

  private static boolean isPublicConstant(Field field) {
    return isPublic(field.getModifiers())
        && isFinal(field.getModifiers())
        && isStatic(field.getModifiers());
  }

  private static class JfrEventType {

    private final String constantName;
    private final String identifier;

    private JfrEventType(String constantName, String identifier) {
      this.constantName = constantName;
      this.identifier = identifier;
    }

    public String getConstantName() {
      return this.constantName;
    }

    public String getIdentifier() {
      return this.identifier;
    }

    @Override
    public String toString() {
      return this.constantName + "=" + this.identifier;
    }
  }

  private static class JfrAttribute {

    private final String constantName;
    private final IAttribute<?> attribute;

    private JfrAttribute(String constantName, IAttribute<?> attribute) {
      this.constantName = constantName;
      this.attribute = attribute;
    }

    public String getConstantName() {
      return this.constantName;
    }

    public IAttribute<?> getAttribute() {
      return this.attribute;
    }

    public String getIdentifier() {
      return this.attribute.getIdentifier();
    }

    @Override
    public String toString() {
      return this.constantName + " -> " + this.attribute.getIdentifier();
    }
  }
}

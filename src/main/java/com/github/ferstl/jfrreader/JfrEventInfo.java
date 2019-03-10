package com.github.ferstl.jfrreader;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;
import org.openjdk.jmc.common.item.IAttribute;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import org.openjdk.jmc.flightrecorder.jdk.JdkTypeIDs;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.toList;

public class JfrEventInfo {

  public static void main(String[] args) throws Exception {
    // Path recording = Paths.get(args[0]);
    // IItemCollection events = JfrLoaderToolkit.loadEvents(recording.toFile());

    // System.out.println(events);

    readEventTypes();
    readAttributes();
  }

  private static List<JfrEventType> readEventTypes() {
    return Stream.of(JdkTypeIDs.class.getFields())
        .filter(field -> isPublicConstant(field) && field.getType() == String.class)
        .map(field -> {
          try {
            return new JfrEventType(field.getName(), (String) field.get(null));
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(toList());
  }

  private static void readAttributes() {
    List<JfrAttribute> attributes = Stream.of(JdkAttributes.class.getFields())
        .filter(field -> isPublicConstant(field) && field.getType() == IAttribute.class)
        .map(field -> {
          try {
            return new JfrAttribute(field.getName(), (IAttribute<?>) field.get(null));
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(toList());

    attributes.forEach(System.out::println);
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

    @Override
    public String toString() {
      return this.constantName + " -> " + this.attribute.getIdentifier();
    }
  }
}

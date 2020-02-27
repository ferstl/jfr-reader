package com.github.ferstl.jfrreader.experiments;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.READ;

public class JfrReader {

  private static final int HEADER_SIZE_BYTES = 68;

  public static void main(String[] args) throws IOException {
    Path recording = Paths.get(args[0]);

    try (var is = Files.newInputStream(recording, READ); var dis = new DataInputStream(is)) {
      //      OutputStream outputStream = Files.newOutputStream(recording.getParent().resolve(recording.getFileName().toString() + "snip"));
      //      outputStream.write(is.readNBytes(15847669));
      //      outputStream.close();
      //      System.exit(0);
      byte[] magic = dis.readNBytes(4);
      int majorVersion = dis.readUnsignedShort();
      int minorVersion = dis.readUnsignedShort();
      long chunkSize = dis.readLong();
      long constantPoolOffset = dis.readLong();
      long metadataOffset = dis.readLong();
      long startTimeNanos = dis.readLong();
      long durationNanos = dis.readLong();
      long startTicks = dis.readLong();
      long ticksPerSecond = dis.readLong();
      int featureFlags = dis.readInt();

      // chunk size contains all header data except the 4 magic start bytes
      byte[] chunkData = dis.readNBytes(((int) chunkSize) - HEADER_SIZE_BYTES);
      byte[] nextChunk = dis.readNBytes(HEADER_SIZE_BYTES);

      System.out.println("Version: " + majorVersion + "." + minorVersion);
      System.out.println("Start Time: " + Instant.ofEpochMilli(startTimeNanos / 1_000_000));
      System.out.println("Duration: " + Duration.ofNanos(durationNanos));
      System.out.println("Compressed Integers: " + ((featureFlags & 0x0001) == 1));
      System.out.println("Ticks per Nanosecond: " + ticksPerSecond / 1_000_000_000.0D);

      DataInputStream cpIs = new DataInputStream(new ByteArrayInputStream(chunkData, (int) constantPoolOffset - HEADER_SIZE_BYTES, chunkData.length - (int) metadataOffset));
      long cpSize = readCompressedLong(cpIs);
      long cpEventType = readCompressedLong(cpIs);
      long cpEventStart = readCompressedLong(cpIs);
      long cpEventDuration = readCompressedLong(cpIs);
      long cpDelta = readCompressedLong(cpIs);
      boolean cpFlush = cpIs.readBoolean();

      long cpClassId = readCompressedLong(cpIs);
      long cpConstantCount = readCompressedLong(cpIs);
      long cpConstantIndex = readCompressedLong(cpIs);


      DataInputStream mdIs = new DataInputStream(new ByteArrayInputStream(chunkData, (int) metadataOffset - HEADER_SIZE_BYTES, chunkData.length - ((int) metadataOffset - HEADER_SIZE_BYTES)));
      long mdSize = readCompressedLong(mdIs);
      long mdEventType = readCompressedLong(mdIs);
      long mdEventStart = readCompressedLong(mdIs);
      long mdEventDuration = readCompressedLong(mdIs);
      long mdId = readCompressedLong(mdIs);
      int stringCount = (int) readCompressedLong(mdIs);

      String[] strings = new String[stringCount];
      for (int i = 0; i < stringCount; i++) {
        byte encoding = mdIs.readByte();
        int length;
        switch (encoding) {
          case 0:
            strings[i] = null;
            break;
          case 1:
            strings[i] = "";
            break;
          case 3:
            length = (int) readCompressedLong(mdIs);
            strings[i] = new String(mdIs.readNBytes(length), UTF_8);
            break;
          case 4:
            length = (int) readCompressedLong(mdIs);
            char[] chars = new char[length];
            for (int j = 0; j < length; j++) {
              chars[j] = (char) readCompressedLong(mdIs);
            }
            strings[i] = new String(chars);
            break;
          case 5:
            length = (int) readCompressedLong(mdIs);
            strings[i] = new String(mdIs.readNBytes(length), ISO_8859_1);
            break;
          default:
            throw new IllegalArgumentException("Unknown String encoding at position " + i + ": " + encoding);
        }
      }


      Node root = readElement(mdIs, strings, 0);
      MetaDataVisitor metaDataVisitor = new MetaDataVisitor();
      root.accept(metaDataVisitor);

      System.out.println("Metadata Size: " + mdSize);

      System.out.println("test");
    }
  }

  private static Node readElement(DataInputStream mdIs, String[] strings, int level) throws IOException {
    Node node = new Node();
    int nameIndex = (int) readCompressedLong(mdIs);
    node.name = strings[nameIndex];
    System.out.println(" ".repeat(level * 2) + node.name);
    int attributeCount = (int) readCompressedLong(mdIs);
    for (int i = 0; i < attributeCount; i++) {
      int keyIndex = (int) readCompressedLong(mdIs);
      int valueIndex = (int) readCompressedLong(mdIs);
      node.attributes.put(strings[keyIndex], strings[valueIndex]);
      System.out.println(" ".repeat(level * 2 + 2) + strings[keyIndex] + " = " + strings[valueIndex]);
    }

    int childElementCount = (int) readCompressedLong(mdIs);
    for (int i = 0; i < childElementCount; i++) {
      node.children.add(readElement(mdIs, strings, level + 1));
    }

    return node;
  }

  private static long readCompressedLong(DataInputStream is) throws IOException {
    long ret = 0;
    for (int i = 0; i < 8; i++) {
      byte b = is.readByte();
      ret += (b & 0x7FL) << (7 * i);
      if (b >= 0) {
        return ret;
      }
    }
    return ret + ((is.readByte() & 0xFFL) << 56);
  }

  static class MetaDataVisitor implements NodeVisitor {

    Map<String, Event> events = new HashMap<>();
    Map<String, AnnotationMetadata> annotations = new HashMap<>();
    Map<String, ClassMetadata> classes = new HashMap<>();
    Map<String, SettingMetadata> settings = new HashMap<>();

    @Override
    public void visit(Node node) {
      if (!"class".equals(node.name)) {
        return;
      }

      String superType = node.attributes.getOrDefault("superType", "n/a");
      switch (superType) {
        case "java.lang.annotation.Annotation":
          processAnnotation(node);
          break;
        case "jdk.jfr.Event":
          processEvent(node);
          break;
        case "jdk.jfr.SettingControl":
          processSetting(node);
          break;
        default:
          processClass(node);
      }
    }

    private void processAnnotation(Node node) {
      String id = node.attributes.get("id");
      AnnotationMetadata annotation = this.annotations.computeIfAbsent(id, key -> new AnnotationMetadata(Integer.parseInt(key)));
      annotation.name = node.attributes.get("name");

      for (Node child : node.children) {
        switch (child.name) {
          case "annotation":
            annotation.annotations.add(createAnnotationValue(child));
            break;
          case "field":
            annotation.fields.add(createField(child));
        }
      }
    }

    private void processEvent(Node node) {
      String id = node.attributes.get("id");
      Event event = this.events.computeIfAbsent(id, key -> new Event(Integer.parseInt(id)));
      event.name = node.attributes.get("name");

      for (Node child : node.children) {
        switch (child.name) {
          case "field":
            event.fields.add(createField(child));
            break;
          case "setting":
            event.settings.add(crateSettingValue(child));
        }
      }
    }

    private void processSetting(Node node) {
      String id = node.attributes.get("id");
      SettingMetadata settingMetadata = this.settings.computeIfAbsent(id, key -> new SettingMetadata(Integer.parseInt(id)));
      settingMetadata.name = node.attributes.get("name");

      for (Node child : node.children) {
        if ("annotation".equals(child.name)) {
          settingMetadata.annotations.add(createAnnotationValue(child));
        }
      }
    }

    private void processClass(Node node) {
      String id = node.attributes.get("id");
      ClassMetadata classMetadata = this.classes.computeIfAbsent(id, key -> new ClassMetadata(Integer.parseInt(id)));
      classMetadata.name = node.attributes.get("name");
      classMetadata.simpleType = Boolean.parseBoolean(node.attributes.getOrDefault("simpleType", "false"));

      for (Node child : node.children) {
        switch (child.name) {
          case "annotation":
            classMetadata.annotations.add(createAnnotationValue(child));
            break;
          case "field":
            classMetadata.fields.add(createField(child));
            break;
        }
      }
    }

    private SettingValue crateSettingValue(Node child) {
      SettingValue value = new SettingValue();
      String metadataId = child.attributes.get("class");

      value.metadata = this.settings.computeIfAbsent(metadataId, key -> new SettingMetadata(Integer.parseInt(key)));
      for (Entry<String, String> entry : child.attributes.entrySet()) {
        String attributeName = entry.getKey();
        String attributeValue = entry.getValue();

        switch (attributeName) {
          case "name":
            value.name = attributeValue;
            break;
          case "defaultValue":
            value.defaultValue = attributeValue;
            break;
        }
      }

      return value;
    }

    private AnnotationValue createAnnotationValue(Node child) {
      AnnotationValue value = new AnnotationValue();
      String annotationTypeId = child.attributes.get("class");

      value.metadata = this.annotations.computeIfAbsent(annotationTypeId, key -> new AnnotationMetadata(Integer.parseInt(key)));
      for (Entry<String, String> entry : child.attributes.entrySet()) {
        String attributeName = entry.getKey();
        String attributeValue = entry.getValue();

        if ("class".equals(attributeName)) {
          continue;
        }

        if (attributeValue.contains("-")) {
          value.addValue(attributeName, attributeValue.substring(0, attributeValue.lastIndexOf('-')));
        }
      }

      return value;
    }

    private FieldMetadata createField(Node child) {
      String name = child.attributes.get("name");
      String typeIdStr = child.attributes.get("class");

      int typeId = Integer.parseInt(typeIdStr);
      FieldMetadata field = new FieldMetadata(typeId);

      field.type = this.classes.computeIfAbsent(typeIdStr, key -> new ClassMetadata(typeId));
      field.name = name;
      field.constantPool = Boolean.parseBoolean(child.attributes.getOrDefault("constantPool", "false"));

      return field;
    }
  }

  static class Node {

    String name;
    Map<String, String> attributes = new HashMap<>();
    List<Node> children = new ArrayList<>();

    void accept(NodeVisitor visitor) {
      visitor.visit(this);
      for (Node child : this.children) {
        child.accept(visitor);
      }
    }
  }

  static interface NodeVisitor {

    void visit(Node node);
  }

  static class BasicMetadata {

    // * includes all primitive types * //
    // !!! Field has no ID !!! //
    final int id;
    String name;

    public BasicMetadata(int id) {
      this.id = id;
    }

  }

  static class AnnotatedMetadata extends BasicMetadata {

    List<AnnotationValue> annotations = new ArrayList<>();

    public AnnotatedMetadata(int id) {
      super(id);
    }

  }

  static class ClassMetadata extends AnnotatedMetadata {

    boolean simpleType;
    List<FieldMetadata> fields = new ArrayList<>();

    public ClassMetadata(int id) {
      super(id);
    }
  }

  static class Event extends ClassMetadata {

    List<SettingValue> settings = new ArrayList<>();

    public Event(int id) {
      super(id);
    }
  }

  static class SettingMetadata extends AnnotatedMetadata {

    public SettingMetadata(int id) {
      super(id);
    }
  }

  static class SettingValue {

    SettingMetadata metadata;
    String name;
    String defaultValue;

  }

  static class AnnotationMetadata extends AnnotatedMetadata {

    public List<FieldMetadata> fields = new ArrayList<>();

    public AnnotationMetadata(int id) {
      super(id);
    }
  }

  static class AnnotationValue {

    AnnotationMetadata metadata;
    Map<String, List<String>> values = new LinkedHashMap<>();

    void addValue(String attributeName, String value) {
      List<String> values = this.values.computeIfAbsent(attributeName, key -> new ArrayList<>());
      values.add(value);
    }
  }

  static class FieldMetadata extends AnnotatedMetadata {

    ClassMetadata type;
    boolean constantPool;

    public FieldMetadata(int id) {
      super(id);
    }
  }
}

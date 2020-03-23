package com.github.ferstl.jfrreader.parser;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.github.ferstl.jfrreader.parser.metadata.ClassInstance;
import com.github.ferstl.jfrreader.parser.metadata.ClassMetadata;
import com.github.ferstl.jfrreader.parser.metadata.ClassMetadataVisitor;
import com.github.ferstl.jfrreader.parser.metadata.EventMetadata;
import com.github.ferstl.jfrreader.parser.metadata.FieldMetadata;
import com.github.ferstl.jfrreader.parser.metadata.MetadataNode;
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
      long lastConstantPoolOffset = dis.readLong();
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

      DataInputStream mdIs = new DataInputStream(new ByteArrayInputStream(chunkData, (int) metadataOffset - HEADER_SIZE_BYTES, chunkData.length - ((int) metadataOffset - HEADER_SIZE_BYTES)));
      long mdSize = readCompressedLong(mdIs);
      long mdEventType = readCompressedLong(mdIs);
      long mdEventStart = readCompressedLong(mdIs);
      long mdEventDuration = readCompressedLong(mdIs);
      long mdId = readCompressedLong(mdIs);
      int stringCount = (int) readCompressedLong(mdIs);

      String[] strings = new String[stringCount];
      for (int i = 0; i < stringCount; i++) {
        strings[i] = readString(mdIs);
      }


      MetadataNode root = readMetadata(mdIs, strings, 0);
      ClassMetadataVisitor classMetaDataVisitor = new ClassMetadataVisitor();
      root.accept(classMetaDataVisitor);

      System.out.println("Metadata Size: " + mdSize);

      long currentConstantPoolOffset = 0;
      long previousConstantPoolOffset = lastConstantPoolOffset;
      while (previousConstantPoolOffset != 0) {
        currentConstantPoolOffset += previousConstantPoolOffset;

        // Constant Pool
        DataInputStream cpIs = new DataInputStream(new ByteArrayInputStream(chunkData, (int) currentConstantPoolOffset - HEADER_SIZE_BYTES, (int) (metadataOffset - currentConstantPoolOffset)));
        long cpSize = readCompressedLong(cpIs);
        long cpEventType = readCompressedLong(cpIs);
        long cpEventStart = readCompressedLong(cpIs);
        long cpEventDuration = readCompressedLong(cpIs);
        previousConstantPoolOffset = readCompressedLong(cpIs);
        boolean cpFlush = cpIs.readBoolean();

        int poolCount = (int) readCompressedLong(cpIs);

        for (int i = 0; i < poolCount; i++) {
          long cpClassId = readCompressedLong(cpIs);
          ClassMetadata classMetadata = classMetaDataVisitor.classes.get("" + cpClassId);
          long cpConstantCount = readCompressedLong(cpIs);
          for (int j = 0; j < cpConstantCount; j++) {
            long constantIndex = readCompressedLong(cpIs);
            ClassInstance classInstance = classMetadata.constants.computeIfAbsent(constantIndex, key -> new ClassInstance(classMetadata));
            for (FieldMetadata field : classMetadata.getFields()) {
              classInstance.addField(field.name, readField(field, cpIs, classMetaDataVisitor.classes));
            }
          }


          //long cpConstantIndex = readCompressedLong(cpIs);
          //System.out.println(cpConstantCount);

        }
      }


      // Body
      DataInputStream bodyIs = new DataInputStream(new ByteArrayInputStream(chunkData, 0, chunkData.length));
      // TODO We need an EventInstance
      List<ClassInstance> events = new ArrayList<>();
      while (true) {
        long size;
        try {
          size = readCompressedLong(bodyIs);
        } catch (EOFException e) {
          // at the moment we don't know, when we are at the end of the stream
          System.out.println("end of chunk reached");
          break;
        }
        long eventType = readCompressedLong(bodyIs);

        if (eventType == 0 || eventType == 1) {
          System.out.println("end of event section " + eventType);
          bodyIs.skipBytes((int) (size - 5)); // size field is padded to 4 bytes + 1 byte for event type
          continue;
        }

        EventMetadata eventMetadata = classMetaDataVisitor.events.get("" + eventType);
        ClassInstance event = new ClassInstance(eventMetadata);
        for (FieldMetadata field : eventMetadata.getFields()) {
          event.addField(field.name, readField(field, bodyIs, classMetaDataVisitor.classes));
        }
        events.add(event);
      }
      System.out.println("test");
    }
  }

  private static String readString(DataInputStream mdIs) throws IOException {
    byte encoding = mdIs.readByte();
    int length;
    switch (encoding) {
      case 0:
        return null;
      case 1:
        return "";
      case 3:
        length = (int) readCompressedLong(mdIs);
        return new String(mdIs.readNBytes(length), UTF_8);
      case 4:
        length = (int) readCompressedLong(mdIs);
        char[] chars = new char[length];
        for (int j = 0; j < length; j++) {
          chars[j] = (char) readCompressedLong(mdIs);
        }
        return new String(chars);
      case 5:
        length = (int) readCompressedLong(mdIs);
        return new String(mdIs.readNBytes(length), ISO_8859_1);
      default:
        throw new IllegalArgumentException("Unknown String encoding : " + encoding);
    }
  }

  private static ClassInstance readField(FieldMetadata field, DataInputStream is, Map<String, ClassMetadata> classes) throws IOException {
    ClassMetadata classMetadata = classes.get("" + field.type.id);
    if (field.constantPool) {
      long constantPoolIndex = readCompressedLong(is);
      return classMetadata.constants.computeIfAbsent(constantPoolIndex, key -> new ClassInstance(classMetadata));
    } else {
      if (classMetadata.getFields().size() > 0) {
        // Instance of the field
        ClassInstance classInstance = new ClassInstance(classMetadata);
        for (FieldMetadata innerField : classMetadata.getFields()) {
          classInstance.addField(innerField.name, readField(innerField, is, classes));
        }
        return classInstance;
      } else {
        ClassInstance classInstance = new ClassInstance(classMetadata);
        switch (classMetadata.name) {
          case "boolean":
            classInstance.value = is.readBoolean();
            break;
          case "char":
            classInstance.value = is.readChar();
            break;
          case "float":
            classInstance.value = is.readFloat();
            break;
          case "double":
            classInstance.value = is.readDouble();
            break;
          case "byte":
            classInstance.value = is.readByte();
            break;
          case "short":
          case "int":
          case "long":
            classInstance.value = readCompressedLong(is);
            break;
          case "java.lang.String":
            classInstance.value = readString(is);
            break;
          default:
            throw new IllegalStateException("Unknown primitive: " + classMetadata.name);
        }
        return classInstance;
      }
    }
  }

  private static MetadataNode readMetadata(DataInputStream mdIs, String[] strings, int level) throws IOException {
    MetadataNode metadataNode = new MetadataNode();
    int nameIndex = (int) readCompressedLong(mdIs);
    metadataNode.name = strings[nameIndex];
    System.out.println(" ".repeat(level * 2) + metadataNode.name);
    int attributeCount = (int) readCompressedLong(mdIs);
    for (int i = 0; i < attributeCount; i++) {
      int keyIndex = (int) readCompressedLong(mdIs);
      int valueIndex = (int) readCompressedLong(mdIs);
      metadataNode.attributes.put(strings[keyIndex], strings[valueIndex]);
      System.out.println(" ".repeat(level * 2 + 2) + strings[keyIndex] + " = " + strings[valueIndex]);
    }

    int childElementCount = (int) readCompressedLong(mdIs);
    for (int i = 0; i < childElementCount; i++) {
      metadataNode.children.add(readMetadata(mdIs, strings, level + 1));
    }

    return metadataNode;
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

}

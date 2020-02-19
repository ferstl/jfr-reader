package com.github.ferstl.jfrreader.experiments;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
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

      System.out.println("Metadata Size: " + mdSize);

      System.out.println("test");
    }
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

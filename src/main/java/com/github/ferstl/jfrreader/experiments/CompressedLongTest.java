package com.github.ferstl.jfrreader.experiments;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class CompressedLongTest {


  public static void main(String[] args) throws IOException {
    System.out.println(bytesToHex(compressLong(Long.MAX_VALUE)));
    System.out.println(bytesToHex(compressLong(-1)));
    System.out.println(bytesToHex(compressLong(1244)));
    System.out.println(bytesToHex(compressLong(12712)));
    System.out.println(bytesToHex(compressLong(4144)));

    System.out.println(readCompressedLong(new byte[]{(byte) 0xDC, (byte) 0x09, (byte) 0x00, (byte) 0x00}));
    System.out.println(readCompressedLong(new byte[]{(byte) 0xDC, (byte) 0x89, (byte) 0x80, (byte) 0x00}));
    System.out.println(readCompressedLong(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F}));
    System.out.println(readCompressedLong(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));
  }

  private static long readCompressedLong(byte[] bytes) throws IOException {
    return readCompressedLong(new DataInputStream(new ByteArrayInputStream(bytes)));
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

  private static byte[] compressLong(long value) {
    byte[] result = new byte[9];
    for (int i = 0; i < 9; i++) {
      result[i] = (byte) (value & 0x7F);
      value >>= 7;

      if (value != 0) {
        result[i] |= (byte) 0x80;
      } else {
        return result;
      }
    }

    return result;
  }

  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }
}

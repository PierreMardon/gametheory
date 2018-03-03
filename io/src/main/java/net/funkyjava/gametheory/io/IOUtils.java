package net.funkyjava.gametheory.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

  private IOUtils() {}

  public static void fill(InputStream is, double[] dest) throws IOException {
    final DataInputStream dis = new DataInputStream(is);
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      dest[i] = dis.readDouble();
    }
  }

  public static void fill(InputStream is, double[][] dest) throws IOException {
    final DataInputStream dis = new DataInputStream(is);
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      final double[] destI = dest[i];
      final int lengthI = destI.length;
      for (int j = 0; j < lengthI; j++) {
        destI[j] = dis.readDouble();
      }
    }
  }

  public static void fill(InputStream is, double[][][] dest) throws IOException {
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      final double[][] destI = dest[i];
      fill(is, destI);
    }
  }

  public static void fill(InputStream is, double[][][][] dest) throws IOException {
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      final double[][][] destI = dest[i];
      fill(is, destI);
    }
  }

  public static void fill(InputStream is, double[][][][][] dest) throws IOException {
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      final double[][][][] destI = dest[i];
      fill(is, destI);
    }
  }

  public static void fill(InputStream is, int[] dest) throws IOException {
    final DataInputStream dis = new DataInputStream(is);
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      dest[i] = dis.readInt();
    }
  }

  public static void fill(InputStream is, int[][] dest) throws IOException {
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      fill(is, dest[i]);
    }
  }

  public static void fill(InputStream is, int[][][] dest) throws IOException {
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      fill(is, dest[i]);
    }
  }

  public static void fill(InputStream is, Fillable[] dest) throws IOException {
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      dest[i].fill(is);
    }
  }

  public static void fill(InputStream is, Fillable[][] dest) throws IOException {
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      fill(is, dest[i]);
    }
  }

  public static void fill(InputStream is, Fillable[][][] dest) throws IOException {
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      fill(is, dest[i]);
    }
  }

  public static void fill(InputStream is, Fillable[][][][] dest) throws IOException {
    final int length = dest.length;
    for (int i = 0; i < length; i++) {
      fill(is, dest[i]);
    }
  }

  public static void write(OutputStream os, double[] src) throws IOException {
    final DataOutputStream dos = new DataOutputStream(os);
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      dos.writeDouble(src[i]);
    }
  }

  public static void write(OutputStream os, double[][] src) throws IOException {
    final DataOutputStream dos = new DataOutputStream(os);
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      final double[] srcI = src[i];
      final int lengthI = srcI.length;
      for (int j = 0; j < lengthI; j++) {
        dos.writeDouble(srcI[j]);
      }
    }
  }

  public static void write(OutputStream os, double[][][] src) throws IOException {
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      final double[][] srcI = src[i];
      write(os, srcI);
    }
  }

  public static void write(OutputStream os, double[][][][] src) throws IOException {
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      final double[][][] srcI = src[i];
      write(os, srcI);
    }
  }

  public static void write(OutputStream os, double[][][][][] src) throws IOException {
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      final double[][][][] srcI = src[i];
      write(os, srcI);
    }
  }

  public static void write(OutputStream os, int[] src) throws IOException {
    final DataOutputStream dos = new DataOutputStream(os);
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      dos.writeInt(src[i]);
    }
  }

  public static void write(OutputStream os, int[][] src) throws IOException {
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      write(os, src[i]);
    }
  }

  public static void write(OutputStream os, int[][][] src) throws IOException {
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      write(os, src[i]);
    }
  }

  public static void write(OutputStream os, Fillable[] src) throws IOException {
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      src[i].write(os);
    }
  }

  public static void write(OutputStream os, Fillable[][] src) throws IOException {
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      write(os, src[i]);
    }
  }

  public static void write(OutputStream os, Fillable[][][] src) throws IOException {
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      write(os, src[i]);
    }
  }

  public static void write(OutputStream os, Fillable[][][][] src) throws IOException {
    final int length = src.length;
    for (int i = 0; i < length; i++) {
      write(os, src[i]);
    }
  }
}

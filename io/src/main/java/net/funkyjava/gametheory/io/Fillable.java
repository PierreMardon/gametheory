package net.funkyjava.gametheory.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Fillable {

  public void fill(InputStream is) throws IOException;

  public void write(OutputStream os) throws IOException;

  public default void fill(String path) throws IOException {
    try (final FileInputStream fis = new FileInputStream(path)) {
      fill(fis);
    }
  }

  public default void write(String path) throws IOException {
    try (final FileOutputStream fos = new FileOutputStream(path)) {
      write(fos);
    }
  }

}

package net.funkyjava.gametheory.gameutil.clustering;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.Getter;
import net.funkyjava.gametheory.io.Fillable;
import net.funkyjava.gametheory.io.IOUtils;

public class Buckets implements Fillable {

  @Getter
  private int nbBuckets;
  @Getter
  private int[] buckets;

  public Buckets() {

  }

  public Buckets(final int nbBuckets, final int[] buckets) {
    this.nbBuckets = nbBuckets;
    this.buckets = buckets;
  }

  @Override
  public void fill(InputStream is) throws IOException {
    final DataInputStream dis = new DataInputStream(is);
    nbBuckets = dis.readInt();
    final int indexSize = dis.readInt();
    buckets = new int[indexSize];
    IOUtils.fill(is, buckets);
  }

  @Override
  public void write(OutputStream os) throws IOException {
    final DataOutputStream dos = new DataOutputStream(os);
    dos.writeInt(nbBuckets);
    dos.writeInt(buckets.length);
    IOUtils.write(os, buckets);
  }

}

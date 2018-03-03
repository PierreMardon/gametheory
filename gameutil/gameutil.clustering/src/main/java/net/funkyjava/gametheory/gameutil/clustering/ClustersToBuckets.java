package net.funkyjava.gametheory.gameutil.clustering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;

public class ClustersToBuckets {

  private ClustersToBuckets() {

  }

  public static <T extends Clusterable> int[] getCanonicalBuckets(
      final List<? extends Cluster<T>> clusters, final List<T> points) {
    final int nbPoints = points.size();
    if (nbPoints == 0) {
      return new int[0];
    }
    final int nbClusters = clusters.size();
    final int[] buckets = new int[nbPoints];

    int count = 0;
    final Map<Integer, Integer> permutations = new HashMap<>();

    pointsLoop: for (int i = 0; i < nbPoints; i++) {
      final T point = points.get(i);
      for (int j = 0; j < nbClusters; j++) {
        if (clusters.get(j).getPoints().contains(point)) {
          Integer bucket = permutations.get(j);
          if (bucket == null) {
            bucket = count++;
            permutations.put(j, bucket);
          }
          buckets[i] = bucket;
          continue pointsLoop;
        }
      }
    }
    return buckets;
  }

  public static <T extends IndexedDoublePoint> Buckets getBucketsForIndexedPoints(
      final List<? extends Cluster<T>> clusters, final List<T> points, boolean canonical) {
    final int nbPoints = points.size();
    if (nbPoints == 0) {
      return null;
    }
    final int nbClusters = clusters.size();
    final int[] buckets = new int[nbPoints];
    for (int clusterIndex = 0; clusterIndex < nbClusters; clusterIndex++) {
      for (final IndexedDoublePoint point : clusters.get(clusterIndex).getPoints()) {
        buckets[point.getIndex()] = clusterIndex;
      }
    }
    if (canonical) {
      final Map<Integer, Integer> permutations = new HashMap<>();
      Integer count = 0;
      for (int i = 0; i < nbPoints; i++) {
        final Integer val = buckets[i];
        Integer perm = permutations.get(val);
        if (perm == null) {
          permutations.put(val, perm = count++);
        }
        buckets[i] = perm;
      }
    }
    return new Buckets(nbClusters, buckets);
  }
}

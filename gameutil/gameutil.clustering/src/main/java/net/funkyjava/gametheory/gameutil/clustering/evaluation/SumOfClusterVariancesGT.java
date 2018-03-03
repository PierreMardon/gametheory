package net.funkyjava.gametheory.gameutil.clustering.evaluation;

import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

public class SumOfClusterVariancesGT<T extends Clusterable> extends ClusterEvaluatorGT<T> {

  /**
   *
   * @param measure the distance measure to use
   */
  public SumOfClusterVariancesGT(final DistanceMeasure measure) {
    super(measure);
  }

  /** {@inheritDoc} */
  @Override
  public double worstScore() {
    return Double.POSITIVE_INFINITY;
  }

  /** {@inheritDoc} */
  @Override
  public double score(final List<? extends Cluster<T>> clusters) {
    double varianceSum = 0.0;
    for (final Cluster<T> cluster : clusters) {
      if (!cluster.getPoints().isEmpty()) {

        final Clusterable center = centroidOf(cluster);

        // compute the distance variance of the current cluster
        final Variance stat = new Variance();
        for (final T point : cluster.getPoints()) {
          stat.increment(distance(point, center));
        }
        varianceSum += stat.getResult();

      }
    }
    return varianceSum;
  }

}

package net.funkyjava.gametheory.gameutil.clustering.evaluation;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.evaluation.ClusterEvaluator;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

public abstract class ClusterEvaluatorGT<T extends Clusterable> extends ClusterEvaluator<T> {

  public ClusterEvaluatorGT(final DistanceMeasure distance) {
    super(distance);
  }

  public abstract double worstScore();
}

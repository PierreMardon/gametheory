package net.funkyjava.gametheory.gameutil.clustering;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.evaluation.ClusterEvaluator;

import net.funkyjava.gametheory.gameutil.clustering.evaluation.ClusterEvaluatorGT;
import net.funkyjava.gametheory.gameutil.clustering.evaluation.SumOfClusterVariancesGT;

/**
 * A wrapper around any clustering algorithm which performs multiple trials and returns the best
 * solution.
 *
 * @param <T> type of the points to cluster
 */
public class MultiClusterer<T extends Clusterable> extends Clusterer<T> {

  /** The underlying clusterer. */
  private final Clusterer<T> clusterer;

  /** The number of trial runs. */
  private final int numTrials;

  /** The cluster evaluator to use. */
  private final ClusterEvaluatorGT<T> evaluator;

  private final int threads;

  /**
   * Build a clusterer.
   *
   * @param clusterer the clusterer to use
   * @param numTrials number of trial runs
   */
  public MultiClusterer(final Clusterer<T> clusterer, final int numTrials) {
    this(clusterer, numTrials, new SumOfClusterVariancesGT<T>(clusterer.getDistanceMeasure()), 1);
  }

  /**
   * Build a clusterer.
   *
   * @param clusterer the clusterer to use
   * @param numTrials number of trial runs
   */
  public MultiClusterer(final Clusterer<T> clusterer, final int numTrials, final int threads) {
    this(clusterer, numTrials, new SumOfClusterVariancesGT<T>(clusterer.getDistanceMeasure()),
        threads);
  }

  /**
   * Build a clusterer.
   *
   * @param clusterer the clusterer to use
   * @param numTrials number of trial runs
   * @param evaluator the cluster evaluator to use
   */
  public MultiClusterer(final Clusterer<T> clusterer, final int numTrials,
      final ClusterEvaluatorGT<T> evaluator, int threads) {
    super(clusterer.getDistanceMeasure());
    checkArgument(threads > 0);
    this.clusterer = clusterer;
    this.numTrials = numTrials;
    this.evaluator = evaluator;
    this.threads = threads;
  }

  /**
   * Returns the embedded clusterer used by this instance.
   *
   * @return the embedded clusterer
   */
  public Clusterer<T> getClusterer() {
    return clusterer;
  }

  /**
   * Returns the number of trials this instance will do.
   *
   * @return the number of trials
   */
  public int getNumTrials() {
    return numTrials;
  }

  /**
   * Returns the {@link ClusterEvaluator} used to determine the "best" clustering.
   *
   * @return the used {@link ClusterEvaluator}
   */
  public ClusterEvaluatorGT<T> getClusterEvaluator() {
    return evaluator;
  }

  /**
   * Runs the clustering algorithm.
   *
   * @param points the points to cluster
   * @return a list of clusters containing the points
   * @throws MathIllegalArgumentException
   * @throws ConvergenceException
   */
  @Override
  public List<? extends Cluster<T>> cluster(final Collection<T> points)
      throws MathIllegalArgumentException, ConvergenceException {
    if (threads > 1) {
      return clusterMultithread(points);
    }
    return clusterMonothread(points);
  }

  protected List<? extends Cluster<T>> clusterMonothread(final Collection<T> points)
      throws MathIllegalArgumentException, ConvergenceException {

    // at first, we have not found any clusters list yet
    List<? extends Cluster<T>> best = null;
    double bestScore = evaluator.worstScore();

    // do several clustering trials
    for (int i = 0; i < numTrials; ++i) {
      // compute a clusters list
      final List<? extends Cluster<T>> clusters = clusterer.cluster(points);
      // compute the score of the current list
      final double score = evaluator.score(clusters);
      if (evaluator.isBetterScore(score, bestScore)) {
        // this one is the best we have found so far, remember it
        best = clusters;
        bestScore = score;
      }

    }

    // return the best clusters list found
    return best;
  }

  private class BestClusters {

    private double score;
    private List<? extends Cluster<T>> clusters;
    private List<Exception> exceptions = new LinkedList<>();

    private BestClusters(double worstScore) {
      score = worstScore;
    }

  }

  protected List<? extends Cluster<T>> clusterMultithread(final Collection<T> points)
      throws MathIllegalArgumentException, ConvergenceException {
    final ExecutorService exe = Executors.newFixedThreadPool(threads);
    // at first, we have not found any clusters list yet
    final BestClusters best = new BestClusters(evaluator.worstScore());

    // do several clustering trials
    for (int i = 0; i < numTrials; ++i) {
      exe.submit(() -> {
        try {
          // compute a clusters list
          List<? extends Cluster<T>> clusters = clusterer.cluster(points);
          // compute the score of the current list
          final double score = evaluator.score(clusters);
          synchronized (best) {
            if (evaluator.isBetterScore(score, best.score)) {
              // this one is the best we have found so far,
              // remember
              // it
              best.clusters = clusters;
              best.score = score;
            }
          }
        } catch (Exception e) {
          synchronized (best) {
            best.exceptions.add(e);
          }
        }
      });
    }
    exe.shutdown();
    try {
      exe.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
    if (!best.exceptions.isEmpty()) {
      throw new IllegalStateException(
          "At least one of MultiClusterer's threads throwed an exception, see the execution traces",
          best.exceptions.get(0));
    }
    // return the best clusters list found
    return best.clusters;
  }
}

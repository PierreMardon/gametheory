package net.funkyjava.gametheory.gameutil.poker.he.clustering;

import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;
import org.apache.commons.math3.ml.neuralnet.FeatureInitializer;
import org.apache.commons.math3.ml.neuralnet.FeatureInitializerFactory;
import org.apache.commons.math3.ml.neuralnet.SquareNeighbourhood;
import org.apache.commons.math3.ml.neuralnet.sofm.LearningFactorFunction;
import org.apache.commons.math3.ml.neuralnet.sofm.LearningFactorFunctionFactory;
import org.apache.commons.math3.ml.neuralnet.sofm.NeighbourhoodSizeFunction;
import org.apache.commons.math3.ml.neuralnet.sofm.NeighbourhoodSizeFunctionFactory;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.clustering.IndexedDoublePoint;
import net.funkyjava.gametheory.gameutil.clustering.MultiClusterer;
import net.funkyjava.gametheory.gameutil.clustering.evaluation.SumOfClusterVariancesGT;
import net.funkyjava.gametheory.gameutil.clustering.neuralnet.KohonenClusterer;
import net.funkyjava.gametheory.gameutil.clustering.neuralnet.NetworkProvider;
import net.funkyjava.gametheory.gameutil.clustering.neuralnet.NeuronSquareMesh2DNetworkProvider;
import net.funkyjava.gametheory.gameutil.clustering.neuralnet.convergence.NeighbourhoodConvergenceMonitorProvider;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.AllHoldemHSTables;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.AllHoldemHSTables.HSType;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.AllHoldemHSTables.Streets;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

@Slf4j
public class PreflopTest {

  @Test
  public void kohonen() {
    final AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> tables =
        AllHoldemHSTables.getTablesWithWaughIndexersTwoPlusTwoEval();
    try {
      tables.fill("/Users/pitt/Documents/PokerData/ALL_HE_HS.zip");
    } catch (Exception e) {
      log.warn("Unable to load Holdem HS Tables", e);
      return;
    }
    final int nbBars = 10;
    final FeatureInitializer init = FeatureInitializerFactory.uniform(0, 0.2);
    final FeatureInitializer[] featureInit = new FeatureInitializer[nbBars];
    for (int i = 0; i < nbBars; i++) {
      featureInit[i] = init;
    }
    final int baseForDecay = 600_000;
    final NeighbourhoodSizeFunction neighbourhoodSize =
        NeighbourhoodSizeFunctionFactory.exponentialDecay(2, 1, baseForDecay * 4);;
    final NetworkProvider networkProvider = new NeuronSquareMesh2DNetworkProvider(4, false, 4,
        false, SquareNeighbourhood.MOORE, featureInit);
    final LearningFactorFunction learningFactor =
        LearningFactorFunctionFactory.exponentialDecay(1, 0.1, baseForDecay);
    final int taskSamplesSize = 10_000;
    final int maxTasks = 100;
    final DistanceMeasure distance = new EarthMoversDistance();
    final NeighbourhoodConvergenceMonitorProvider convergenceMonitorProvider =
        new NeighbourhoodConvergenceMonitorProvider(neighbourhoodSize);
    final Clusterer<IndexedDoublePoint> baseClusterer =
        new KohonenClusterer<>(distance, learningFactor, neighbourhoodSize, networkProvider,
            taskSamplesSize, maxTasks, new JDKRandomGenerator(), convergenceMonitorProvider);
    final MultiClusterer<IndexedDoublePoint> multiClusterer =
        new MultiClusterer<>(baseClusterer, 4, 3);
    final HoldemHSClusterer clusterer = HoldemHSClusterer.clustererForNextStreetHSHistograms(tables,
        Streets.PREFLOP, HSType.EHS, nbBars, multiClusterer);
    final SumOfClusterVariancesGT<IndexedDoublePoint> evaluator =
        new SumOfClusterVariancesGT<>(distance);
    log.debug("Starting preflop's flops EHS histograms clustering with Kohonen SOM");
    List<? extends Cluster<IndexedDoublePoint>> clusters = clusterer.cluster();
    log.debug("Obtained buckets :");
    log.debug("");
    clusterer.printPreflop2DBuckets(clusters, clusterer.getPoints());
    final double score = evaluator.score(clusters);
    log.debug("Score : {}", score);
  }

  @Test
  public void kmeans() {
    final AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> tables =
        AllHoldemHSTables.getTablesWithWaughIndexersTwoPlusTwoEval();
    try {
      tables.fill("/Users/pitt/Documents/PokerData/ALL_HE_HS.zip");
    } catch (Exception e) {
      log.warn("Unable to load Holdem HS Tables", e);
      return;
    }
    final int nbBars = 10;

    final int taskSamplesSize = 10_000;
    final int maxTasks = 100;
    final DistanceMeasure distance = new EarthMoversDistance();

    final Clusterer<IndexedDoublePoint> baseClusterer =
        new KMeansPlusPlusClusterer<>(12, taskSamplesSize * maxTasks);
    final MultiClusterer<IndexedDoublePoint> multiClusterer =
        new MultiClusterer<>(baseClusterer, 4, 4);
    final HoldemHSClusterer clusterer = HoldemHSClusterer.clustererForNextStreetHSHistograms(tables,
        Streets.PREFLOP, HSType.EHS, nbBars, multiClusterer);
    final SumOfClusterVariancesGT<IndexedDoublePoint> evaluator =
        new SumOfClusterVariancesGT<>(distance);
    log.debug("Starting preflop's flops EHS histograms clustering with k-means++");
    List<? extends Cluster<IndexedDoublePoint>> clusters = clusterer.cluster();
    log.debug("Obtained buckets :");
    log.debug("");
    clusterer.printPreflop2DBuckets(clusters, clusterer.getPoints());
    final double score = evaluator.score(clusters);
    log.debug("Score : {}", score);
  }

  @Test
  public void kmeans2() {
    final AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> tables =
        AllHoldemHSTables.getTablesWithWaughIndexersTwoPlusTwoEval();
    try {
      tables.fill("/Users/pitt/Documents/PokerData/ALL_HE_HS.zip");
    } catch (Exception e) {
      log.warn("Unable to load Holdem HS Tables", e);
      return;
    }

    final int taskSamplesSize = 10_000;
    final int maxTasks = 100;
    final DistanceMeasure distance = new EarthMoversDistance();

    final Clusterer<IndexedDoublePoint> baseClusterer =
        new KMeansPlusPlusClusterer<>(12, taskSamplesSize * maxTasks);
    final MultiClusterer<IndexedDoublePoint> multiClusterer =
        new MultiClusterer<>(baseClusterer, 4, 4);
    final HoldemHSClusterer clusterer =
        HoldemHSClusterer.clustererForStreetHS(tables, Streets.PREFLOP, HSType.EHS, multiClusterer);
    final SumOfClusterVariancesGT<IndexedDoublePoint> evaluator =
        new SumOfClusterVariancesGT<>(distance);
    log.debug("Starting preflop's EHS values clustering with k-means++");
    List<? extends Cluster<IndexedDoublePoint>> clusters = clusterer.cluster();
    log.debug("Obtained buckets :");
    log.debug("");
    clusterer.printPreflop2DBuckets(clusters, clusterer.getPoints());
    final double score = evaluator.score(clusters);
    log.debug("Score : {}", score);
  }
}

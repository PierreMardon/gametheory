package net.funkyjava.gametheory.gameutil.poker.he.clustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import com.google.common.base.Optional;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.clustering.Buckets;
import net.funkyjava.gametheory.gameutil.clustering.ClustersToBuckets;
import net.funkyjava.gametheory.gameutil.clustering.IndexedDoublePoint;
import net.funkyjava.gametheory.gameutil.clustering.MultiClusterer;
import net.funkyjava.gametheory.gameutil.clustering.evaluation.SumOfClusterVariancesGT;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.AllHoldemHSTables;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.AllHoldemHSTables.HSType;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.AllHoldemHSTables.Streets;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.HoldemHSHistograms;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;
import net.funkyjava.gametheory.io.ProgramArguments;

@Slf4j
public class HoldemHSClusterer {

  private final List<IndexedDoublePoint> points;
  private final Clusterer<IndexedDoublePoint> clusterer;
  private final Streets street;

  private HoldemHSClusterer(
      final AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> tables,
      final Streets street, final HSType nextStreetHSType, final int nbBars,
      final Clusterer<IndexedDoublePoint> clusterer) {
    this.street = street;
    this.clusterer = clusterer;
    final double[][] vectors =
        HoldemHSHistograms.generateHistograms(tables, street, nextStreetHSType, nbBars);
    final int nbPoints = vectors.length;
    final List<IndexedDoublePoint> points = new ArrayList<>(nbPoints);
    for (int i = 0; i < nbPoints; i++) {
      points.add(new IndexedDoublePoint(vectors[i], i));
    }
    this.points = Collections.unmodifiableList(points);
  }

  private HoldemHSClusterer(
      final AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> tables,
      final Streets street, final HSType hsType, final Clusterer<IndexedDoublePoint> clusterer) {
    this.street = street;
    this.clusterer = clusterer;
    final double[] table = tables.getTable(street, hsType);
    final int nbPoints = table.length;
    final List<IndexedDoublePoint> points = new ArrayList<>(nbPoints);
    for (int i = 0; i < nbPoints; i++) {
      points.add(new IndexedDoublePoint(new double[] {table[i]}, i));
    }
    this.points = Collections.unmodifiableList(points);
  }

  public List<? extends Cluster<IndexedDoublePoint>> cluster() {
    return clusterer.cluster(points);
  }

  public static HoldemHSClusterer clustererForNextStreetHSHistograms(
      final AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> tables,
      final Streets street, final HSType nextStreetHSType, final int nbBars,
      final Clusterer<IndexedDoublePoint> clusterer) {
    return new HoldemHSClusterer(tables, street, nextStreetHSType, nbBars, clusterer);
  }

  public static HoldemHSClusterer clustererForStreetHS(
      final AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> tables,
      final Streets street, final HSType hsType, final Clusterer<IndexedDoublePoint> clusterer) {
    return new HoldemHSClusterer(tables, street, hsType, clusterer);
  }

  public <T extends Clusterable> int[][] getCanonicalPreflop2DBuckets(
      final List<? extends Cluster<T>> clusters, final List<T> points) {
    if (street != Streets.PREFLOP) {
      throw new IllegalStateException("Wrong street " + street);
    }
    final int nbClusters = clusters.size();
    final int[][] buckets = new int[13][13];
    final WaughIndexer indexer = new WaughIndexer(new int[] {2});
    final IntCardsSpec cardsSpec = indexer.getCardsSpec();
    final int[][] cardsGroups = new int[][] {{0, 0}};
    final int[] cards = cardsGroups[0];
    final Map<Integer, Integer> permutations = new HashMap<>();
    int count = 0;
    for (int rank1 = 0; rank1 < 13; rank1++) {
      pointsLoop: for (int rank2 = 0; rank2 < 13; rank2++) {
        if (rank1 <= rank2) {
          // Off suite or pair
          cards[0] = cardsSpec.getCard(rank1, 0);
          cards[1] = cardsSpec.getCard(rank2, 1);
        } else if (rank2 < rank1) {
          // Suited
          cards[0] = cardsSpec.getCard(rank1, 0);
          cards[1] = cardsSpec.getCard(rank2, 0);
        }
        final T point = points.get(indexer.indexOf(cardsGroups));
        for (int j = 0; j < nbClusters; j++) {
          if (clusters.get(j).getPoints().contains(point)) {
            Integer bucket = permutations.get(j);
            if (bucket == null) {
              bucket = count++;
              permutations.put(j, bucket);
            }
            buckets[rank1][rank2] = bucket;
            continue pointsLoop;
          }
        }
      }
    }
    return buckets;
  }

  public <T extends Clusterable> void printPreflop2DBuckets(
      final List<? extends Cluster<T>> clusters, final List<T> points) {
    if (street != Streets.PREFLOP) {
      throw new IllegalStateException("Wrong street " + street);
    }
    final int[][] buckets = getCanonicalPreflop2DBuckets(clusters, points);
    final WaughIndexer indexer = new WaughIndexer(new int[] {2});
    final IntCardsSpec cardsSpec = indexer.getCardsSpec();
    final Cards52Strings strings = new Cards52Strings(cardsSpec);
    System.out.print("\t");
    for (int topRank = 0; topRank < 13; topRank++) {
      System.out.print(strings.getRankStr(cardsSpec.getCard(topRank, 0)) + "\t");
    }
    System.out.println();
    for (int rank2 = 0; rank2 < 13; rank2++) {
      System.out.print(strings.getRankStr(cardsSpec.getCard(rank2, 0)) + "\t");
      for (int rank1 = 0; rank1 < 13; rank1++) {
        System.out.print(buckets[rank1][rank2] + "\t");
      }
      System.out.println();
    }
  }

  public List<IndexedDoublePoint> getPoints() {
    return points;
  }

  private static final String nextStreetPrefix = "nextStreet=";
  private static final String streetPrefix = "street=";
  private static final String hsTypePrefix = "hsType=";
  private static final String tablesPrefix = "tables=";
  private static final String nbBarsPrefix = "nbBars=";
  private static final String kPrefix = "k=";
  private static final String maxIterationsPrefix = "maxIter=";
  private static final String numTrialsPrefix = "numTrials=";
  private static final String destPrefix = "dest=";

  public static void main(String[] args) throws IOException {
    Optional<AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer>> optTables =
        tables(args);
    if (!optTables.isPresent()) {
      return;
    }
    final Optional<String> destOpt = ProgramArguments.getArgument(args, destPrefix);
    if (!destOpt.isPresent()) {
      log.info("No destination path found, the resulting buckets won't be saved");
    }
    final Optional<Integer> kOpt = ProgramArguments.getStrictlyPositiveIntArgument(args, kPrefix);
    if (!kOpt.isPresent()) {
      return;
    }
    final Optional<Integer> maxIterationsOpt =
        ProgramArguments.getStrictlyPositiveIntArgument(args, maxIterationsPrefix);
    if (!maxIterationsOpt.isPresent()) {
      return;
    }
    final Optional<Integer> numTrials =
        ProgramArguments.getStrictlyPositiveIntArgument(args, numTrialsPrefix);
    if (!numTrials.isPresent()) {
      return;
    }
    final AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> tables =
        optTables.get();
    final Optional<Streets> nextStreetArg = streetFromArgs(args, nextStreetPrefix);
    final boolean isNextStreet = nextStreetArg.isPresent();
    final DistanceMeasure measure =
        isNextStreet ? new EarthMoversDistance() : new EuclideanDistance();
    final Clusterer<IndexedDoublePoint> baseClusterer =
        new KMeansPlusPlusClusterer<>(kOpt.get(), maxIterationsOpt.get(), measure);
    final SumOfClusterVariancesGT<IndexedDoublePoint> evaluator =
        new SumOfClusterVariancesGT<>(measure);
    final Clusterer<IndexedDoublePoint> clusterer = new MultiClusterer<>(baseClusterer,
        numTrials.get(), evaluator, Runtime.getRuntime().availableProcessors());
    final Optional<HSType> hsTypeOpt = typeFromArgs(args, hsTypePrefix);
    if (!hsTypeOpt.isPresent()) {
      return;
    }
    HoldemHSClusterer hsClusterer;
    if (isNextStreet) {
      final Optional<Integer> nbBarsOpt = ProgramArguments.getIntArgument(args, nbBarsPrefix);
      if (!nbBarsOpt.isPresent()) {
        return;
      }
      hsClusterer = clustererForNextStreetHSHistograms(tables, nextStreetArg.get(), hsTypeOpt.get(),
          nbBarsOpt.get(), clusterer);
    } else {
      final Optional<Streets> streetArg = streetFromArgs(args, streetPrefix);
      if (!streetArg.isPresent()) {
        return;
      }
      hsClusterer = clustererForStreetHS(tables, streetArg.get(), hsTypeOpt.get(), clusterer);
    }
    log.info("Clustering...");
    final long start = System.currentTimeMillis();
    List<? extends Cluster<IndexedDoublePoint>> clusters = hsClusterer.cluster();
    final long time = System.currentTimeMillis() - start;
    log.info("Took {} ms", time);
    log.info("Evaluating score...");
    log.info("{}", evaluator.score(clusters));
    log.info("Creating buckets");
    final Buckets buckets =
        ClustersToBuckets.getBucketsForIndexedPoints(clusters, hsClusterer.getPoints(), false);
    if (destOpt.isPresent()) {
      final String path = destOpt.get();
      log.info("Saving buckets at {}", path);
      buckets.write(path);
    }
  }

  private static Optional<AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer>> tables(
      final String[] args) {
    final Optional<String> tablesPath = ProgramArguments.getArgument(args, tablesPrefix);
    if (!tablesPath.isPresent()) {
      log.error("No path to load HS tables");
      return Optional.absent();
    }
    final AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> tables =
        AllHoldemHSTables.getTablesWithWaughIndexersTwoPlusTwoEval();
    try {
      tables.fill(tablesPath.get());
      return Optional.of(tables);
    } catch (IOException e) {
      log.error("Couldn't read tables at path {}", tablesPath.get());
      return Optional.absent();
    }
  }

  private static Optional<HSType> typeFromArgs(final String[] args, final String argPrefix) {
    final Optional<String> strArgOpt = ProgramArguments.getArgument(args, argPrefix);
    if (!strArgOpt.isPresent()) {
      return Optional.absent();
    }
    try {
      return Optional.of(HSType.valueOf(strArgOpt.get()));
    } catch (IllegalArgumentException e) {
      log.error("HS Type \"{}\" is unknown, should be in {}", strArgOpt.get(), HSType.values());
      return Optional.absent();
    }
  }

  private static Optional<Streets> streetFromArgs(final String[] args, final String argPrefix) {
    final Optional<String> strArgOpt = ProgramArguments.getArgument(args, argPrefix);
    if (!strArgOpt.isPresent()) {
      return Optional.absent();
    }
    try {
      return Optional.of(Streets.valueOf(strArgOpt.get()));
    } catch (IllegalArgumentException e) {
      log.error("Street \"{}\" is unknown, should be in {}", strArgOpt.get(), Streets.values());
      return Optional.absent();
    }
  }
}

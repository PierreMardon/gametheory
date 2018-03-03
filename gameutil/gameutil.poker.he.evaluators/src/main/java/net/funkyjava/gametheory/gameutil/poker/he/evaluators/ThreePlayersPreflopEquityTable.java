package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.mutable.MutableLong;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;
import net.funkyjava.gametheory.io.Fillable;
import net.funkyjava.gametheory.io.IOUtils;

@Slf4j
public class ThreePlayersPreflopEquityTable implements Fillable {

  private static final int[] onePlayerGroupsSize = {2};
  private static final int[] threePlayersGroupsSize = {2, 2, 2};

  @Getter
  private final WaughIndexer threePlayersIndexer = new WaughIndexer(threePlayersGroupsSize);
  @Getter
  private final int nbPreflopThreePlayers = threePlayersIndexer.getIndexSize();
  @Getter
  private final WaughIndexer holeCardsIndexer = new WaughIndexer(onePlayerGroupsSize);
  @Getter
  private final int nbHoleCards = holeCardsIndexer.getIndexSize();

  @Getter
  private final double[][][] equities = new double[nbPreflopThreePlayers][][];

  private boolean computed = false;

  public ThreePlayersPreflopEquityTable() {

  }

  public boolean isComputed() {
    return computed;
  }

  public void compute() throws InterruptedException {
    checkState(!isComputed(), "Tables have already been computed");
    computeAccurateEquities();
    computed = true;
  }

  private final void computeAccurateEquities() throws InterruptedException {
    new TwoPlusTwoEvaluator(); // Just to load it before we get started
    final long start = System.currentTimeMillis();
    final MutableLong enqueued = new MutableLong();
    final ExecutorService exe =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    final WaughIndexer threePlayersIndexer = this.threePlayersIndexer;
    final WaughIndexer holeCardsIndexer = this.holeCardsIndexer;
    final double[][][] equities = this.equities;
    final int nbPreflopThreePlayers = this.nbPreflopThreePlayers;
    for (int index = 0; index < nbPreflopThreePlayers; index++) {
      final int finalIndex = index;
      final int[][] holeCards = new int[3][2];
      threePlayersIndexer.unindex(index, holeCards);
      final int h1Index = holeCardsIndexer.indexOf(new int[][] {holeCards[0]});
      final int h2Index = holeCardsIndexer.indexOf(new int[][] {holeCards[1]});
      if (h1Index > h2Index) {
        continue;
      }
      final int h3Index = holeCardsIndexer.indexOf(new int[][] {holeCards[2]});
      if (h2Index > h3Index) {
        continue;
      }

      final double[][] handEquities = new double[4][3];
      equities[finalIndex] = handEquities;
      exe.execute(() -> {
        final double[][] res = new ThreePlayersEquityRawEvaluator(holeCardsIndexer.getCardsSpec(),
            new TwoPlusTwoEvaluator()).getValues(holeCards[0], holeCards[1], holeCards[2]);
        for (int i = 0; i < 4; i++) {
          System.arraycopy(res[i], 0, handEquities[i], 0, 3);
        }
        synchronized (enqueued) {
          enqueued.decrement();
          if (enqueued.getValue() < 100) {
            enqueued.notify();
          }
        }

      });
      synchronized (enqueued) {
        enqueued.increment();
        if (index != nbPreflopThreePlayers - 1 && enqueued.longValue() >= 1000) {
          enqueued.wait();
          final double ratioDone = index / (double) nbPreflopThreePlayers;
          final double elapsed = System.currentTimeMillis() - start;
          log.info("Remaining time {} minutes",
              (int) (elapsed * (1 - ratioDone) / (60 * 1000 * ratioDone)));
        }
      }
    }
    log.info("Feeder : end enqueuing runnables, awaiting termination");
    exe.shutdown();
    exe.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    log.info("All runnables were executed");
  }

  public IntCardsSpec getCardsSpec() {
    return DefaultIntCardsSpecs.getDefault();
  }

  private static final byte nullArray = 0;
  private static final byte filledArray = 1;

  @Override
  public void fill(InputStream is) throws IOException {
    final double[][][] equities = this.equities;
    final int nbPreflopThreePlayers = this.nbPreflopThreePlayers;
    try (final DataInputStream dis = new DataInputStream(is)) {
      for (int i = 0; i < nbPreflopThreePlayers; i++) {
        final byte b = dis.readByte();
        if (b == nullArray) {
          continue;
        }
        final double[][] handEquities = new double[4][3];
        equities[i] = handEquities;
        IOUtils.fill(dis, handEquities);
      }
    }
    computed = true;
  }

  @Override
  public void write(OutputStream os) throws IOException {
    final double[][][] equities = this.equities;
    final int nbPreflopThreePlayers = this.nbPreflopThreePlayers;
    try (final DataOutputStream dos = new DataOutputStream(os)) {
      for (int i = 0; i < nbPreflopThreePlayers; i++) {
        if (equities[i] == null) {
          dos.writeByte(nullArray);
          continue;
        }
        dos.writeByte(filledArray);
        final double[][] handEquities = equities[i];
        IOUtils.write(dos, handEquities);
      }
    }
  }

  public static void main(String[] args) {
    checkArgument(args.length == 1, "3 Players Preflop Tables writing misses a path argument");
    final String pathStr = args[0];
    final Path path = Paths.get(pathStr);
    checkArgument(!Files.exists(path),
        "File " + path.toAbsolutePath().toString() + " already exists");
    try (final FileOutputStream fos = new FileOutputStream(path.toFile())) {
      final ThreePlayersPreflopEquityTable tables = new ThreePlayersPreflopEquityTable();
      tables.compute();
      tables.write(fos);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}

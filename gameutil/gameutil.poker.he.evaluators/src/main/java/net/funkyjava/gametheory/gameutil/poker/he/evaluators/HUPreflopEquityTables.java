package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.mutable.MutableLong;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.CardsGroupsDrawingTask;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

@Slf4j
public class HUPreflopEquityTables implements Serializable {

  private static final long serialVersionUID = 1808572853794466312L;
  private static final int[] onePlayerGroupsSize = {2};
  private static final int[] twoPlayersGroupsSize = {2, 2};

  @Getter
  private final WaughIndexer twoPlayersIndexer = new WaughIndexer(twoPlayersGroupsSize);
  @Getter
  private final int nbPreflopTwoPlayers = twoPlayersIndexer.getIndexSize();
  @Getter
  private final WaughIndexer holeCardsIndexer = new WaughIndexer(onePlayerGroupsSize);
  @Getter
  private final int nbHoleCards = holeCardsIndexer.getIndexSize();
  @Getter
  private final double[][] reducedEquity = new double[nbHoleCards][nbHoleCards];
  @Getter
  private final int[][] reducedCounts = new int[nbHoleCards][nbHoleCards];
  @Getter
  private final double[] equity = new double[nbPreflopTwoPlayers];

  private long total = 0;
  private long start;

  public boolean isComputed() {
    return equity[0] != 0;
  }

  public synchronized void compute() throws InterruptedException {
    checkState(!isComputed(), "Tables have already been computed");
    computeAccurateEquity();
    computeReducedEquity();
  }

  private final void computeAccurateEquity() throws InterruptedException {
    final double[] equity = this.equity;
    final MutableLong done = new MutableLong();
    final ExecutorService exe =
        Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
    final WaughIndexer holeCardsIndexer = new WaughIndexer(new int[] {2, 2});

    final int nbHoleCards = this.nbPreflopTwoPlayers;

    start = System.currentTimeMillis();
    for (int index = 0; index < nbHoleCards; index++) {
      if (equity[index] != 0) {
        continue;
      }
      final int finalIndex = index;
      final TwoPlusTwoEvaluator eval = new TwoPlusTwoEvaluator();
      final Cards52SpecTranslator translateToEval =
          new Cards52SpecTranslator(holeCardsIndexer.getCardsSpec(), eval.getCardsSpec());
      final int[][] holeCards = new int[2][2];
      holeCardsIndexer.unindex(index, holeCards);
      final int[][] reversedHoleCards = new int[][] {holeCards[1], holeCards[0]};
      final int reversedIndex = holeCardsIndexer.indexOf(reversedHoleCards);
      translateToEval.translate(holeCards);
      final int[] heroCards = new int[7];
      heroCards[0] = holeCards[0][0];
      heroCards[1] = holeCards[0][1];
      final int[] vilainCards = new int[7];
      vilainCards[0] = holeCards[1][0];
      vilainCards[1] = holeCards[1][1];

      final Deck52Cards evalDeck = new Deck52Cards(eval.getCardsSpec());
      final int[] WLT = new int[3];
      // Just to set it reserved
      equity[index] = 1;
      equity[reversedIndex] = 1;
      total++;
      exe.execute(() -> {
        evalDeck.drawAllGroupsCombinations(new int[] {5}, (CardsGroupsDrawingTask) cardsGroups -> {
          final int[] board = cardsGroups[0];
          System.arraycopy(board, 0, heroCards, 2, 5);
          System.arraycopy(board, 0, vilainCards, 2, 5);
          final int heroVal = eval.get7CardsEval(heroCards);
          final int vilainVal = eval.get7CardsEval(vilainCards);
          if (heroVal > vilainVal) {
            WLT[0]++;
          } else if (heroVal < vilainVal) {
            WLT[1]++;
          } else {
            WLT[2]++;
          }
          return true;
        }, holeCards[0], holeCards[1]);
        final double win = WLT[0];
        final double lose = WLT[1];
        final double tie = WLT[2];
        final double eq = (win + tie / 2d) / (win + lose + tie);
        equity[finalIndex] = eq;
        equity[reversedIndex] = 1 - eq;
        synchronized (done) {
          done.increment();
          final long doneLong = done.longValue();
          final double ratioDone = doneLong / (double) total;
          if (doneLong % 1000 == 0 && ratioDone != 0 && ratioDone != 1) {
            final long elapsed = System.currentTimeMillis() - start;
            log.info("Remaining operations {}/{}, time {}s", total - doneLong, total,
                (int) (elapsed * (1 - ratioDone) / (1000 * ratioDone)));
          }
        }
      });
    }

    log.info("Put {} runnables", total);
    exe.shutdown();
    exe.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
  }

  private void computeReducedEquity() {
    final IntCardsSpec indexSpecs = holeCardsIndexer.getCardsSpec();
    final double[] equity = this.equity;
    final double[][] reducedEquity = this.reducedEquity;
    final int[][] reducedCounts = this.reducedCounts;
    final Deck52Cards deck = new Deck52Cards(indexSpecs);
    final WaughIndexer onePlayerIndexer = new WaughIndexer(onePlayerGroupsSize);
    final WaughIndexer twoPlayersIndexer = new WaughIndexer(twoPlayersGroupsSize);
    deck.drawAllGroupsCombinations(twoPlayersGroupsSize, (CardsGroupsDrawingTask) cardsGroups -> {

      final int indexInTables = twoPlayersIndexer.indexOf(cardsGroups);
      final int heroIndex = onePlayerIndexer.indexOf(new int[][] {cardsGroups[0]});
      final int vilainIndex = onePlayerIndexer.indexOf(new int[][] {cardsGroups[1]});
      final double eq = equity[indexInTables];
      reducedEquity[heroIndex][vilainIndex] += eq;
      reducedCounts[heroIndex][vilainIndex]++;
      return true;
    });
    final int nbHoleCards = this.nbHoleCards;
    for (int i = 0; i < nbHoleCards; i++) {
      final double[] eq1 = reducedEquity[i];
      final int[] c1 = reducedCounts[i];
      for (int j = 0; j < nbHoleCards; j++) {
        eq1[j] /= c1[j];
      }
    }
  }

  public double getEquity(int[] heroCards, int[] opponentCards) {
    return equity[twoPlayersIndexer.indexOf(new int[][] {heroCards, opponentCards})];
  }

  public double getReducedEquity(final int[] heroCards, final int[] vilainCards) {
    return reducedEquity[holeCardsIndexer.indexOf(new int[][] {heroCards})][holeCardsIndexer
        .indexOf(new int[][] {vilainCards})];
  }

  public int getReducedCount(final int[] heroCards, final int[] vilainCards) {
    return reducedCounts[holeCardsIndexer.indexOf(new int[][] {heroCards})][holeCardsIndexer
        .indexOf(new int[][] {vilainCards})];
  }

  public IntCardsSpec getCardsSpec() {
    return DefaultIntCardsSpecs.getDefault();
  }

  public static void main(String[] args) {
    checkArgument(args.length == 1, "HU Preflop Tables writing misses a path argument");
    final String pathStr = args[0];
    final Path path = Paths.get(pathStr);
    checkArgument(!Files.exists(path),
        "File " + path.toAbsolutePath().toString() + " already exists");
    try (final ObjectOutputStream oos =
        new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
      final HUPreflopEquityTables tables = new HUPreflopEquityTables();
      tables.compute();
      oos.writeObject(tables);
      oos.flush();
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

}

package net.funkyjava.gametheory.gameutil.poker.he.clustering;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.CardsGroupsDrawingTask;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.CardsGroupsIndexer;
import net.funkyjava.gametheory.gameutil.cards.indexing.CardsGroupsIndexerProvider;
import net.funkyjava.gametheory.gameutil.clustering.Buckets;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.AllHoldemHSTables.Streets;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluatorProvider;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

/**
 *
 * Class to build histograms of hand strength versus opponent clustered hole cards (OCHS) as shown
 * here : http://poker.cs.ualberta.ca/publications/AAMAS13-abstraction.pdf
 *
 * @author Pierre Mardon
 *
 */
@Slf4j
public class HoldemOpponentClusterHandStrength {

  private HoldemOpponentClusterHandStrength() {}

  private static final void computeFor(final CardsGroupsIndexer preflopIndexer,
      final int[] preflopBuckets, final int nbBuckets, final int[][] streetHeroCards,
      final int nbBoardCards, final int nbMissingBoardCards, final double[] res,
      final Holdem7CardsEvaluator eval, final AtomicLong count, final long streetSize,
      final long startTime) {
    final IntCardsSpec cardsSpec = DefaultIntCardsSpecs.getDefault();
    final Cards52SpecTranslator translateToEval =
        new Cards52SpecTranslator(cardsSpec, eval.getCardsSpec());
    final Deck52Cards deck = new Deck52Cards(0);
    final int[] heroHoleCards = streetHeroCards[0];
    final int[] streetBoardCards = streetHeroCards[1];
    final int[] heroCardsEval = new int[7];
    final int[] oppCardsEval = new int[7];
    heroCardsEval[0] = translateToEval.translate(heroHoleCards[0]);
    heroCardsEval[1] = translateToEval.translate(heroHoleCards[1]);
    for (int j = 0; j < nbBoardCards; j++) {
      heroCardsEval[2 + j] = translateToEval.translate(streetBoardCards[j]);
      oppCardsEval[2 + j] = translateToEval.translate(streetBoardCards[j]);
    }
    final long[] win = new long[nbBuckets];
    final long[] lose = new long[nbBuckets];
    final long[] tie = new long[nbBuckets];
    if (nbMissingBoardCards > 0) {
      deck.drawAllGroupsCombinations(new int[] {2, nbMissingBoardCards},
          (CardsGroupsDrawingTask) cardsGroups -> {
            final int[] opHole = cardsGroups[0];
            final int bucket = preflopBuckets[preflopIndexer.indexOf(new int[][] {opHole})];
            oppCardsEval[0] = translateToEval.translate(opHole[0]);
            oppCardsEval[1] = translateToEval.translate(opHole[1]);
            final int[] missingBoard = cardsGroups[1];
            for (int j = 0; j < nbMissingBoardCards; j++) {
              final int index = j + nbBoardCards;
              oppCardsEval[index] =
                  heroCardsEval[index] = translateToEval.translate(missingBoard[j]);
            }
            final int heroEval = eval.get7CardsEval(heroCardsEval);
            final int oppEval = eval.get7CardsEval(oppCardsEval);
            if (heroEval < oppEval) {
              lose[bucket]++;
            } else if (heroEval > oppEval) {
              win[bucket]++;
            } else {
              tie[bucket]++;
            }
            return true;
          }, streetHeroCards);
    } else {
      deck.drawAllGroupsCombinations(new int[] {2}, (CardsGroupsDrawingTask) cardsGroups -> {
        final int[] opHole = cardsGroups[0];
        final int bucket = preflopBuckets[preflopIndexer.indexOf(new int[][] {opHole})];
        oppCardsEval[0] = translateToEval.translate(opHole[0]);
        oppCardsEval[1] = translateToEval.translate(opHole[1]);
        final int heroEval = eval.get7CardsEval(heroCardsEval);
        final int oppEval = eval.get7CardsEval(oppCardsEval);
        if (heroEval < oppEval) {
          lose[bucket]++;
        } else if (heroEval > oppEval) {
          win[bucket]++;
        } else {
          tie[bucket]++;
        }
        return true;
      }, streetHeroCards);
    }
    for (int j = 0; j < nbBuckets; j++) {
      res[j] = (win[j] + tie[j] / 2.0d) / (win[j] + tie[j] + lose[j]);
    }
    final long currentCount = count.incrementAndGet();
    final double time = System.currentTimeMillis();
    final double ratio = ((double) currentCount) / (double) streetSize;
    final double dur = time - startTime;
    final double timePerIter = dur / currentCount;
    final double timeRemaining = timePerIter * (streetSize - currentCount);
    log.debug("{}/{} {}% {}s/iter {}s remaining", currentCount, streetSize, ratio * 100,
        timePerIter / 1000, timeRemaining / 1000);
  }

  public static double[][] opponentClusterHandStrength(
      final CardsGroupsIndexerProvider<? extends CardsGroupsIndexer> preflopIndexerProvider,
      final Buckets buckets, final Streets street, final WaughIndexer streetIndexer,
      final Holdem7CardsEvaluatorProvider evalProvider) throws InterruptedException {
    final AtomicLong count = new AtomicLong();
    final long startTime = System.currentTimeMillis();
    final int nbProcs = Runtime.getRuntime().availableProcessors();
    final ExecutorService exe = Executors.newFixedThreadPool(nbProcs);
    final int streetIndexSize = streetIndexer.getIndexSize();

    log.info("Computing OCHS for street {}, index size {} nb processors {}", street,
        streetIndexSize, nbProcs);
    final int nbBuckets = buckets.getNbBuckets();
    final double[][] res = new double[streetIndexSize][nbBuckets];
    int tmpNbBoardCards = 0;
    switch (street) {
      case FLOP:
        tmpNbBoardCards = 3;
        break;
      case PREFLOP:
        break;
      case RIVER:
        tmpNbBoardCards = 5;
        break;
      case TURN:
        tmpNbBoardCards = 4;
        break;
    }
    final int nbBoardCards = tmpNbBoardCards;
    final int nbMissingBoardCards = 5 - nbBoardCards;
    for (int i = 0; i < streetIndexSize; i++) {
      final int[] heroHoleCards = new int[2];
      final int[] streetBoardCards = new int[nbBoardCards];
      final int[][] streetHeroCards = {heroHoleCards, streetBoardCards};
      final double[] indexRes = res[i];
      streetIndexer.unindex(i, streetHeroCards);
      exe.execute(new Runnable() {

        @Override
        public void run() {
          computeFor(preflopIndexerProvider.getIndexer(), buckets.getBuckets(), nbBuckets,
              streetHeroCards, nbBoardCards, nbMissingBoardCards, indexRes,
              evalProvider.getEvaluator(), count, streetIndexSize, startTime);
        }
      });
    }
    log.info("All executables enqueued");
    exe.shutdown();
    exe.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    return res;
  }
}

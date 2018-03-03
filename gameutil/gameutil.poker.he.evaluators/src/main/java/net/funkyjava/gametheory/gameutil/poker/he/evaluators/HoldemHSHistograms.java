package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.commons.lang3.mutable.MutableLong;

import net.funkyjava.gametheory.gameutil.cards.CardsGroupsDrawingTask;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.AllHoldemHSTables.HSType;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.AllHoldemHSTables.Streets;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

/**
 *
 * HS histograms, practically used to cluster using k-means and earth-mover distance as shown here :
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.295.2143&rep=rep1& type=pdf
 *
 * @author Pierre Mardon
 *
 */
public class HoldemHSHistograms {

  public static double[][] generateHistograms(
      final AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> tables,
      final Streets street, final HSType nextStreetValue, final int numberOfBars) {
    WaughIndexer streetIndexer;
    WaughIndexer nextStreetIndexer;
    int numberOfCardsToAddForNextStreet;
    switch (street) {
      case FLOP:
        streetIndexer = tables.getFlopCardsIndexer();
        nextStreetIndexer = tables.getTurnCardsIndexer();
        numberOfCardsToAddForNextStreet = 1;
        break;
      case PREFLOP:
        streetIndexer = tables.getHoleCardsIndexer();
        nextStreetIndexer = tables.getFlopCardsIndexer();
        numberOfCardsToAddForNextStreet = 3;
        break;
      case TURN:
        streetIndexer = tables.getTurnCardsIndexer();
        nextStreetIndexer = tables.getRiverCardsIndexer();
        numberOfCardsToAddForNextStreet = 1;
        break;
      default:
        throw new IllegalArgumentException("Impossible case");
    }
    double[] nextStreetValues = tables.getTable(street.getNextStreet(), nextStreetValue);
    return generateHistograms(streetIndexer, nextStreetIndexer, nextStreetValues,
        numberOfCardsToAddForNextStreet, numberOfBars);
  }

  private static double[][] generateHistograms(final WaughIndexer streetIndexer,
      final WaughIndexer nextStreetIndexer, final double[] nextStreetValues,
      final int numberOfCardsToAddForNextStreet, final int numberOfBars) {
    final int streetSize = streetIndexer.getIndexSize();
    checkArgument(nextStreetValues.length == nextStreetIndexer.getIndexSize(),
        "Next street values count != next street indexer size");
    final long[] streetCards = new long[2];
    final long[] nextStreetCards = new long[2];
    final double[][] histograms = new double[streetSize][numberOfBars];
    final Deck52Cards deck = new Deck52Cards(0);
    for (int streetIdx = 0; streetIdx < streetSize; streetIdx++) {
      final double[] vector = histograms[streetIdx];
      streetIndexer.unindex(streetIdx, streetCards);
      nextStreetCards[0] = streetCards[0];
      final long deckMask = streetCards[0] | streetCards[1];
      final MutableLong nbHits = new MutableLong();
      deck.drawAllGroupsCombinations(new int[] {numberOfCardsToAddForNextStreet},
          (CardsGroupsDrawingTask) cardsGroups -> {
            final int[] cards = cardsGroups[0];
            long nextStreetMask = 0l;
            for (int i = 0; i < numberOfCardsToAddForNextStreet; i++) {
              final int card = cards[i];
              nextStreetMask |= 0x1l << (16 * (card / 13) + card % 13);
              if ((nextStreetMask & deckMask) != 0) {
                return true;
              }
            }
            nextStreetCards[1] = nextStreetMask | streetCards[1];
            final int nextStreetIndex = nextStreetIndexer.index(nextStreetCards);
            vector[(int) Math.round(nextStreetValues[nextStreetIndex] * (numberOfBars - 1))]++;
            nbHits.increment();
            return true;
          });
      final double hits = nbHits.doubleValue();
      for (int i = 0; i < numberOfBars; i++) {
        vector[i] /= hits;
      }
    }
    return histograms;
  }

}

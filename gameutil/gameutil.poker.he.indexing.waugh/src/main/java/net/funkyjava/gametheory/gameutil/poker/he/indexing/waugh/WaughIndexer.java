package net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.Ranks13BitsCommon.combinations;
import static net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.Ranks13BitsCommon.msbIndexes;
import static net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.Ranks13BitsCommon.msbMasks;
import static net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.Ranks13BitsCommon.numberOfRanks;
import static net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.Ranks13BitsCommon.numberOfSetBits;

import java.io.Serializable;
import java.util.Arrays;

import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.CardsGroupsIndexer;

/**
 *
 * Kevin Waugh's "Fast and Optimal Hand Isomorphism Algorithm" implementation
 *
 * For mono-thread use only because we avoid all object creations after instantiation.
 *
 * @see <a href= "https://www.aaai.org/ocs/index.php/WS/AAAIW13/paper/download/7042/6491"> his
 *      paper</a>
 * @see <a href="http://poker-ai.org/phpbb/viewtopic.php?f=25&t=2660">the topic on Poker-AI.org</a>
 * @see <a href="http://www.cs.cmu.edu/~kwaugh/">his website</a>
 *
 * @author Pierre Mardon
 *
 */
public final class WaughIndexer implements CardsGroupsIndexer, Serializable {

  private static final long serialVersionUID = 4961591218585744214L;
  public static final DefaultIntCardsSpecs cardsSpecs = DefaultIntCardsSpecs.getDefault();
  private static final int nbColors = 4;

  private final int[] confOffsets;
  private final ColorsConfiguration[] confs;
  private final int size;
  private final int nbCardsGroups;
  private final int nbOfConfsPerColor;
  private final int[] cardsGroupsSizes;

  private final int[][] tmpCards;
  private final int[][] tmpConf;
  private final int[] tmpColorsIdxs;
  private final int maxNbOfConfs;
  private final int[] groupsSizes;
  private final long[] groupsCards;

  /**
   * Constructor.
   *
   * @param cardsGroupsSizes
   */
  public WaughIndexer(final int[] cardsGroupsSizes) {
    this.groupsSizes =
        checkNotNull(cardsGroupsSizes, "Cards groups sizes argument cannot be null").clone();
    checkArgument(cardsGroupsSizes.length > 0,
        "Don't supply an empty array for cards groups sizes !");
    nbCardsGroups = cardsGroupsSizes.length;
    this.groupsCards = new long[nbCardsGroups];
    int nbOfConfsPerColor = 1;
    for (int i = 0; i < nbCardsGroups; i++) {
      checkArgument(cardsGroupsSizes[i] > 0, "All groups of cards must have a size > 0");
      nbOfConfsPerColor *= cardsGroupsSizes[i] + 1;
    }
    this.nbOfConfsPerColor = nbOfConfsPerColor;
    int maxNbOfConfs = 1;
    for (int i = 0; i < nbColors; i++) {
      maxNbOfConfs *= nbOfConfsPerColor;
    }
    this.maxNbOfConfs = maxNbOfConfs;
    this.confs = new ColorsConfiguration[maxNbOfConfs];
    this.confOffsets = new int[maxNbOfConfs];
    for (int i = 0; i < maxNbOfConfs; i++) {
      confOffsets[i] = -1;
    }
    this.cardsGroupsSizes = cardsGroupsSizes.clone();
    this.tmpConf = new int[nbColors][nbCardsGroups];

    this.size = enumForColor(0, 0);
    if (size < 0) {
      throw new IllegalArgumentException(
          "It seems that the groups sizes you provided are too big and the index exceeds integer max value "
              + Integer.MAX_VALUE);
    }
    tmpCards = new int[nbColors][nbCardsGroups];
    tmpColorsIdxs = new int[nbColors];
  }

  // Returns cumulative size
  private int enumForColor(final int color, final int currentOffset) {
    if (color == nbColors) {
      // Stop there, if the conf is valid create it, set its offset and
      // return currentOffset + its size, else return currentOffset
      if (!isConfValid()) {
        return currentOffset;
      }
      final ColorsConfiguration cc = new ColorsConfiguration(tmpConf);
      int ccIndex = 0;

      for (int i = 0; i < nbColors; i++) {
        int colorConfIndex = 0;
        for (int j = 0; j < nbCardsGroups; j++) {
          colorConfIndex = tmpConf[i][j] + (cardsGroupsSizes[j] + 1) * colorConfIndex;
        }
        ccIndex = colorConfIndex + nbOfConfsPerColor * ccIndex;
      }
      if (confs[ccIndex] != null) {
        throw new IllegalArgumentException("Colors configuration colision. Existing : "
            + Arrays.deepToString(confs[ccIndex].orderedColorsGroupsConf) + " new "
            + Arrays.deepToString(tmpConf) + " with same index " + ccIndex);
      }
      confs[ccIndex] = cc;
      confOffsets[ccIndex] = currentOffset;
      if (cc.getSize() < 0 || cc.getSize() + currentOffset < 0) {
        throw new IllegalArgumentException(
            "It seems that the groups sizes you provided are too big and the index exceeds integer max value "
                + Integer.MAX_VALUE);
      }
      return currentOffset + cc.getSize();
    }
    return enumForGroup(color, 0, currentOffset);
  }

  private int enumForGroup(final int color, final int group, int currentOffset) {
    if (group == nbCardsGroups) {
      return enumForColor(color + 1, currentOffset);
    }
    int offset = currentOffset;
    for (int nbCards = 0; nbCards <= cardsGroupsSizes[group]; nbCards++) {
      tmpConf[color][group] = nbCards;
      offset = enumForGroup(color, group + 1, offset);
    }
    return offset;
  }

  private final boolean isConfValid() {
    int[] groupsCardinal = new int[nbCardsGroups];
    for (int i = 0; i < nbColors; i++) {
      // Check lexicographic order
      if (i > 0 && !colorConfIsGreater(tmpConf[i - 1], tmpConf[i])) {
        return false;
      }
      for (int j = 0; j < nbCardsGroups; j++) {
        groupsCardinal[j] += tmpConf[i][j];
      }
    }
    return Arrays.equals(groupsCardinal, cardsGroupsSizes);
  }

  private final boolean colorConfIsGreater(int[] greater, int[] lower) {
    for (int i = 0; i < nbCardsGroups; i++) {
      if (greater[i] < lower[i]) {
        return false;
      }
      if (greater[i] > lower[i]) {
        return true;
      }
    }

    return true;
  }

  /**
   * Index cards groups. For performance, no check is done on the number of cards or groups or their
   * validity. The behavior of this method is not specified for invalid arguments.
   *
   * Groups are represented as long with bits i * 16 + (0 to 12) representing the presence of the
   * ranks for the arbitrary color i.
   *
   * @param groupsCards the cards of the groups to index
   * @return the index
   */
  public final int index(long[] groupsCards) {
    int i = 0, j, k, l, m, mult;
    int ranksUsed, set, setIdx, nbRanks, msbMask;
    int[] arrVar1;
    int[] arrVar2;
    final int[] nbOfSetBits = numberOfSetBits;
    final int[][] comb = combinations;
    final int[] colorsIdx = tmpColorsIdxs;
    final int[][] conf = tmpConf;
    final int[] msbMasksArr = msbMasks;
    final int[][] cards = tmpCards;
    final int nbGroups = nbCardsGroups;
    final int[] msbIndices = msbIndexes;
    final int nbOfRanks = numberOfRanks;
    final int colorsCount = nbColors;
    final int[] groupsSizes = cardsGroupsSizes;

    // Set the cards in tmpCards, the colors configurations in tmpConf and
    // the colors idxs in tmpColorsIdxs, and order them by conf
    // lexicographic order and idxs for lexicographic equality
    for (; i < colorsCount; i++) {
      arrVar1 = conf[i];
      arrVar2 = cards[i];
      for (j = 0; j < nbGroups; j++) {
        arrVar1[j] = nbOfSetBits[arrVar2[j] = (int) (groupsCards[j] >> (16 * i)) & 0xFFFF];
      }

      // Index this color's group
      // Hardly readable due to var reuse, see
      // PokerRanksGroupsIndexing.indexGroup
      m = ranksUsed = 0;
      mult = 1;

      for (k = 0; k < nbGroups; k++) {
        set = arrVar2[k];
        setIdx = 0;
        nbRanks = nbOfSetBits[set];
        for (j = 0; j < nbRanks; j++) {
          msbMask = msbMasksArr[set];
          setIdx += comb[msbIndices[set] - nbOfSetBits[(msbMask - 1) & ranksUsed]][nbRanks - j]; // +
                                                                                                 // 1
                                                                                                 // ?
          set ^= msbMask;
        }
        m += setIdx * mult;
        mult *= comb[nbOfRanks - nbOfSetBits[ranksUsed]][nbRanks];
        ranksUsed |= arrVar2[k];
      }
      colorsIdx[i] = m;

      // Eventually swap the color conf/cards and idx with the previous
      // one based on lexicographic order then idxs order
      j = i - 1;
      orderLoop: while (j >= 0) {
        arrVar1 = conf[j + 1];
        arrVar2 = conf[j];
        for (k = 0; k < nbGroups; k++) {
          if (arrVar1[k] < arrVar2[k]) {
            // Already ordered
            break orderLoop;
          } else if (arrVar1[k] > arrVar2[k]
              || (k == nbGroups - 1 && colorsIdx[j + 1] > colorsIdx[j])) {
            // Order those two elements
            conf[j] = arrVar1;
            conf[j + 1] = arrVar2;

            l = colorsIdx[j];
            colorsIdx[j] = colorsIdx[j + 1];
            colorsIdx[j + 1] = l;

            j--;
            continue orderLoop;
          } // Else just continue
        }

        // the two sets (last and previous) have same conf and last one
        // has its idx <= previous one : break the order loop
        break;
      }
    }
    // Find the ColorsConfiguration object to index the indexes set
    // Hardly readable, see ColorsConfiguration instantiation in
    // enumForColor method
    m = 0;
    for (i = 0; i < colorsCount; i++) {
      arrVar1 = conf[i];
      k = 0;
      for (j = 0; j < nbGroups; j++) {
        k = arrVar1[j] + (groupsSizes[j] + 1) * k;
      }
      m = k + nbOfConfsPerColor * m;
    }

    return confOffsets[m] + confs[m].indexIdxsForConf(colorsIdx);
  }

  /**
   * Retrieves canonical groups of card represented by this index
   *
   * Groups are represented as long with bits i * 16 + (0 to 12) representing the presence of the
   * ranks for the arbitrary color i.
   *
   * @param idx the index
   * @param dest the destination array that must have a sufficient length
   */
  public final void unindex(final int idx, final long[] dest) {
    int maxOffsetIndex = -1;
    int maxOffsetValue = -1;
    int i = 0;
    for (; i < maxNbOfConfs; i++) {
      if (confOffsets[i] < 0 || confOffsets[i] > idx || confOffsets[i] < maxOffsetValue) {
        continue;
      }
      maxOffsetValue = confOffsets[i];
      maxOffsetIndex = i;
    }
    final ColorsConfiguration cc = confs[maxOffsetIndex];
    cc.unindexIdxsForConf(idx - confOffsets[maxOffsetIndex], tmpColorsIdxs);
    for (i = 0; i < nbColors; i++) {
      PokerRanksGroupsIndexing.unindexGroup(tmpColorsIdxs[i], cc.orderedColorsGroupsConf[i],
          tmpCards[i]);
    }
    for (int j = 0; j < nbCardsGroups; j++) {
      dest[j] = 0l;
      for (i = 0; i < nbColors; i++) {
        dest[j] |= ((long) tmpCards[i][j]) << (16 * i);
      }
    }
  }

  public final void unindex(final int idx, final int[][] dest) {
    int maxOffsetIndex = -1;
    int maxOffsetValue = -1;
    int i = 0;
    for (; i < maxNbOfConfs; i++) {
      if (confOffsets[i] < 0 || confOffsets[i] > idx || confOffsets[i] < maxOffsetValue) {
        continue;
      }
      maxOffsetValue = confOffsets[i];
      maxOffsetIndex = i;
    }
    final ColorsConfiguration cc = confs[maxOffsetIndex];
    cc.unindexIdxsForConf(idx - confOffsets[maxOffsetIndex], tmpColorsIdxs);
    for (i = 0; i < nbColors; i++) {
      PokerRanksGroupsIndexing.unindexGroup(tmpColorsIdxs[i], cc.orderedColorsGroupsConf[i],
          tmpCards[i]);
    }
    for (int j = 0; j < nbCardsGroups; j++) {
      int cardIndex = 0;
      for (i = 0; i < nbColors; i++) {
        int groupRanks = tmpCards[i][j];
        while (groupRanks != 0) {
          final int rank = msbIndexes[groupRanks];
          groupRanks ^= (0x1 << rank);
          dest[j][cardIndex++] = rank * nbColors + i;
        }
      }
    }
  }

  /**
   * Index cards groups. For performance, no check is done on the number of cards or groups or their
   * validity. The behavior of this method is not specified for invalid arguments.
   *
   * Groups must be represented according to the {@link IntCardsSpec} returned by
   * {@link #getCardsSpec()}.
   *
   * For performance, it's better to use the {@link #index(long[])} if your cards are already
   * represented as bit masks.
   *
   * @param cardsGroups the cards of the groups to index
   * @return the index
   */
  @Override
  public int indexOf(int[][] cardsGroups) {
    int i = 0, j, k, l, m, mult;

    int ranksUsed, set, setIdx, nbRanks, msbMask;
    int[] arrVar1;
    int[] arrVar2;

    final int[] nbOfSetBits = numberOfSetBits;
    final int[][] comb = combinations;
    final int[] colorsIdx = tmpColorsIdxs;
    final int[][] conf = tmpConf;
    final int[] msbMasksArr = msbMasks;
    final int[][] cards = tmpCards;
    final int nbGroups = nbCardsGroups;
    final int[] msbIndices = msbIndexes;
    final int nbOfRanks = numberOfRanks;
    final int colorsCount = nbColors;
    final int[] groupsSizes = cardsGroupsSizes;
    final long[] gCards = groupsCards;

    for (; i < nbGroups; i++) {
      gCards[i] = 0l;
      arrVar1 = cardsGroups[i];
      for (j = 0; j < groupsSizes[i]; j++) {
        gCards[i] |= 0x1l << ((arrVar1[j] / 4) + 16 * (arrVar1[j] % 4));
      }
    }
    i = 0;

    // Set the cards in tmpCards, the colors configurations in tmpConf and
    // the colors idxs in tmpColorsIdxs, and order them by conf
    // lexicographic order and idxs for lexicographic equality
    for (; i < colorsCount; i++) {
      arrVar1 = conf[i];
      arrVar2 = cards[i];
      for (j = 0; j < nbGroups; j++) {
        arrVar1[j] = nbOfSetBits[arrVar2[j] = (int) (gCards[j] >> (16 * i)) & 0xFFFF];
      }

      // Index this color's group
      // Hardly readable due to var reuse, see
      // PokerRanksGroupsIndexing.indexGroup
      m = ranksUsed = 0;
      mult = 1;

      for (k = 0; k < nbGroups; k++) {
        set = arrVar2[k];
        setIdx = 0;
        nbRanks = nbOfSetBits[set];
        for (j = 0; j < nbRanks; j++) {
          msbMask = msbMasksArr[set];
          setIdx += comb[msbIndices[set] - nbOfSetBits[(msbMask - 1) & ranksUsed]][nbRanks - j]; // +
                                                                                                 // 1
                                                                                                 // ?
          set ^= msbMask;
        }
        m += setIdx * mult;
        mult *= comb[nbOfRanks - nbOfSetBits[ranksUsed]][nbRanks];
        ranksUsed |= arrVar2[k];
      }
      colorsIdx[i] = m;
      // Eventually swap the color conf/cards and idx with the previous
      // one based on lexicographic order then idxs order
      j = i - 1;
      orderLoop: while (j >= 0) {
        arrVar1 = conf[j + 1];
        arrVar2 = conf[j];
        for (k = 0; k < nbGroups; k++) {
          if (arrVar1[k] < arrVar2[k]) {
            // Already ordered
            break orderLoop;
          } else if (arrVar1[k] > arrVar2[k]
              || (k == nbGroups - 1 && colorsIdx[j + 1] > colorsIdx[j])) {
            // Order those two elements
            conf[j] = arrVar1;
            conf[j + 1] = arrVar2;

            l = colorsIdx[j];
            colorsIdx[j] = colorsIdx[j + 1];
            colorsIdx[j + 1] = l;

            j--;
            continue orderLoop;
          } // Else just continue
        }

        // the two sets (last and previous) have same conf and last one
        // has its idx <= previous one : break the order loop
        break;
      }
    }
    // Find the ColorsConfiguration object to index the indexes set
    // Hardly readable, see ColorsConfiguration instantiation in
    // enumForColor method
    m = 0;
    for (i = 0; i < colorsCount; i++) {
      arrVar1 = conf[i];
      k = 0;
      for (j = 0; j < nbGroups; j++) {
        k = arrVar1[j] + (groupsSizes[j] + 1) * k;
      }
      m = k + nbOfConfsPerColor * m;
    }

    return confOffsets[m] + confs[m].indexIdxsForConf(colorsIdx);
  }

  @Override
  public int getIndexSize() {
    return size;
  }

  @Override
  public IntCardsSpec getCardsSpec() {
    return cardsSpecs;
  }

  @Override
  public boolean canHandleGroups(int[] groupsSizes) {
    return Arrays.equals(groupsSizes, this.groupsSizes);
  }

  @Override
  public boolean isCompatible(String gameId) {
    // By default true because it's compatible with many games as long as
    // colors are arbitrary
    return true;
  }
}

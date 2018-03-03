package net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LongColorsConfiguration {
  private static final int numberOfColors = 4;

  private final long[] colorsGroupsSizes;
  private final long[] colorsFullGroupsSizes;
  private final long size;
  private final int[] sameConfColorsCount;
  private final long[] indexMult;
  private final int numberOfDistinctConfs;
  final int[][] orderedColorsGroupsConf;

  public LongColorsConfiguration(int[][] orderedColorsGroupsConf) {
    this.orderedColorsGroupsConf = new int[orderedColorsGroupsConf.length][];
    for (int i = 0; i < orderedColorsGroupsConf.length; i++) {
      this.orderedColorsGroupsConf[i] =
          Arrays.copyOf(orderedColorsGroupsConf[i], orderedColorsGroupsConf[i].length);
    }
    final List<int[]> groups = new ArrayList<>();
    final List<Integer> consecutiveColorsSameConf = new ArrayList<>();
    for (int i = 0; i < numberOfColors; i++) {
      if (i > 0 && Arrays.equals(orderedColorsGroupsConf[i], groups.get(groups.size() - 1))) {
        int index = groups.size() - 1;
        consecutiveColorsSameConf.set(index, consecutiveColorsSameConf.get(index) + 1);
        continue;
      }
      groups.add(orderedColorsGroupsConf[i]);
      consecutiveColorsSameConf.add(1);
    }
    colorsGroupsSizes = new long[groups.size()];
    colorsFullGroupsSizes = new long[groups.size()];
    long size = 1;
    for (int i = 0; i < colorsGroupsSizes.length; i++) {
      colorsGroupsSizes[i] = PokerRanksGroupsIndexing.sizeForGroups(groups.get(i));
      if (colorsGroupsSizes[i] > 0) {
        size *= colorsFullGroupsSizes[i] =
            combination(colorsGroupsSizes[i] + consecutiveColorsSameConf.get(i) - 1,
                consecutiveColorsSameConf.get(i));
      }
    }
    this.size = size;
    numberOfDistinctConfs = consecutiveColorsSameConf.size();
    sameConfColorsCount = new int[numberOfDistinctConfs];
    for (int i = 0; i < consecutiveColorsSameConf.size(); i++) {
      sameConfColorsCount[i] = consecutiveColorsSameConf.get(i);
    }
    indexMult = new long[numberOfDistinctConfs];
    int mult = 1;
    for (int i = 0; i < sameConfColorsCount.length; i++) {
      indexMult[i] = mult;
      mult *=
          combination(colorsGroupsSizes[i] + sameConfColorsCount[i] - 1, sameConfColorsCount[i]);
    }

  }

  private static final long multisetColex(final long[] idxs, final int offset, final int length) {
    // Idxs must be ordered from greatest to lowest
    int res = 0;
    int i = 0;
    for (; i < length - 1; i++) {
      res += combination(idxs[i + offset] + length - i - 1, length - i);
    }
    return res + idxs[i + offset];
  }

  private final void multisetUncolex(final long idx, final long[] destIdxs, final int offset,
      final int groupConfIndex) {
    final int length = sameConfColorsCount[groupConfIndex];
    if (length == 1) {
      destIdxs[offset] = idx;
      return;
    }
    long newIdx = idx;
    long maxNextIndex = colorsGroupsSizes[groupConfIndex];
    for (int remainingIndexes = length; remainingIndexes > 0; remainingIndexes--) {
      for (; combination(maxNextIndex + remainingIndexes - 1,
          remainingIndexes) > newIdx; maxNextIndex--) {
        ;
      }
      destIdxs[offset + length - remainingIndexes] = maxNextIndex;
      newIdx -= combination(maxNextIndex + remainingIndexes - 1, remainingIndexes);
    }
  }

  private static final long combination(final long n, final long k) {
    if (n < k || k <= 0) {
      return 0;
    }
    if (n == k) {
      return 1;
    }
    long dividend = 1;
    long quotient = 1;
    for (long i = 1; i <= k; i++) {
      dividend *= n - i + 1;
      quotient *= i;
    }
    return dividend / quotient;
  }

  public final int oldIndexIdxsForConf(final long[] colorsIdxs) {
    int res = 0;
    int offset = 0;
    for (int i = 0; i < numberOfDistinctConfs; i++) {
      res += indexMult[i] * multisetColex(colorsIdxs, offset, sameConfColorsCount[i]);
      offset += sameConfColorsCount[i];
    }
    return res;
  }

  public final long indexIdxsForConf(final long[] colorsIdxs) {
    long res = 0;
    int offset = 0;
    long tmpColex;
    int j;
    int length;
    long n;
    int k;
    long l;
    long quot;
    long div;
    final long nbDistinctConfs = numberOfDistinctConfs;
    final int[] sameConfCount = sameConfColorsCount;
    final long[] mult = indexMult;
    for (int i = 0; i < nbDistinctConfs; i++) {
      tmpColex = j = 0;
      length = sameConfCount[i];
      for (; j < length - 1; j++) {
        if ((k = length - j) == 0) {
          continue;
        }
        if ((n = colorsIdxs[j + offset] + length - j - 1) == k) {
          tmpColex++;
          continue;
        }
        for (div = quot = l = 1; l <= k; l++) {
          div *= n - l + 1;
          quot *= l;
        }
        tmpColex += div / quot;
      }
      res += mult[i] * (tmpColex + colorsIdxs[j + offset]);
      offset += sameConfCount[i];
    }
    return res;
  }

  public final void unindexIdxsForConf(final long idx, final long[] colorsIdxsDest) {
    long tmpIdx = idx;
    int j = 0;
    for (int i = 0; i < numberOfDistinctConfs; i++) {
      multisetUncolex(tmpIdx % colorsFullGroupsSizes[i], colorsIdxsDest, j, i);
      tmpIdx /= colorsFullGroupsSizes[i];
      j += sameConfColorsCount[i];
    }
  }

  public final long getSize() {
    return size;
  }
}

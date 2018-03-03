package net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorsConfiguration implements Serializable {

  private static final long serialVersionUID = -376350138103708955L;

  private static final int numberOfColors = 4;

  private final int[] colorsGroupsSizes;
  private final int[] colorsFullGroupsSizes;
  private final int size;
  private final int[] sameConfColorsCount;
  private final int[] indexMult;
  private final int numberOfDistinctConfs;
  final int[][] orderedColorsGroupsConf;

  public ColorsConfiguration(int[][] orderedColorsGroupsConf) {
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
    colorsGroupsSizes = new int[groups.size()];
    colorsFullGroupsSizes = new int[groups.size()];
    numberOfDistinctConfs = consecutiveColorsSameConf.size();
    int size = 1;
    for (int i = 0; i < numberOfDistinctConfs; i++) {
      colorsGroupsSizes[i] = PokerRanksGroupsIndexing.sizeForGroups(groups.get(i));
      size *= colorsFullGroupsSizes[i] =
          combination(colorsGroupsSizes[i] + consecutiveColorsSameConf.get(i) - 1,
              consecutiveColorsSameConf.get(i));
    }
    this.size = size;
    sameConfColorsCount = new int[numberOfDistinctConfs];
    for (int i = 0; i < consecutiveColorsSameConf.size(); i++) {
      sameConfColorsCount[i] = consecutiveColorsSameConf.get(i);
    }
    indexMult = new int[numberOfDistinctConfs];
    int mult = 1;
    for (int i = 0; i < sameConfColorsCount.length; i++) {
      indexMult[i] = mult;
      mult *=
          combination(colorsGroupsSizes[i] + sameConfColorsCount[i] - 1, sameConfColorsCount[i]);
    }

  }

  private static final int multisetColex(final int[] idxs, final int offset, final int length) {
    // Idxs must be ordered from greatest to lowest
    int res = 0;
    int i = 0;
    for (; i < length - 1; i++) {
      res += combination(idxs[i + offset] + length - i - 1, length - i);
    }
    return res + idxs[i + offset];
  }

  private final void multisetUncolex(final int idx, final int[] destIdxs, final int offset,
      final int groupConfIndex) {
    final int length = sameConfColorsCount[groupConfIndex];
    if (length == 1) {
      destIdxs[offset] = idx;
      return;
    }
    int newIdx = idx;
    int maxNextIndex = colorsGroupsSizes[groupConfIndex];
    for (int remainingIndexes = length; remainingIndexes > 0; remainingIndexes--) {
      for (; combination(maxNextIndex + remainingIndexes - 1,
          remainingIndexes) > newIdx; maxNextIndex--) {
        ;
      }
      destIdxs[offset + length - remainingIndexes] = maxNextIndex;
      newIdx -= combination(maxNextIndex + remainingIndexes - 1, remainingIndexes);
    }
  }

  private static final int combination(final int n, final int k) {
    if (n < k || k <= 0) {
      return 0;
    }
    if (n == k) {
      return 1;
    }
    int dividend = 1;
    int quotient = 1;
    for (int i = 1; i <= k; i++) {
      dividend *= n - i + 1;
      quotient *= i;
    }
    return dividend / quotient;
  }

  public final int oldIndexIdxsForConf(final int[] colorsIdxs) {
    int res = 0;
    int offset = 0;
    for (int i = 0; i < numberOfDistinctConfs; i++) {
      res += indexMult[i] * multisetColex(colorsIdxs, offset, sameConfColorsCount[i]);
      offset += sameConfColorsCount[i];
    }
    return res;
  }

  public final int indexIdxsForConf(final int[] colorsIdxs) {
    int res = 0;
    int offset = 0;
    int tmpColex;
    int j;
    int length;
    int n;
    int k;
    int l;
    int quot;
    int div;
    final int nbDistinctConfs = numberOfDistinctConfs;
    final int[] sameConfCount = sameConfColorsCount;
    final int[] mult = indexMult;
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

  public final void unindexIdxsForConf(final int idx, final int[] colorsIdxsDest) {
    int tmpIdx = idx;
    int j = 0;
    for (int i = 0; i < numberOfDistinctConfs; i++) {
      multisetUncolex(tmpIdx % colorsFullGroupsSizes[i], colorsIdxsDest, j, i);
      tmpIdx /= colorsFullGroupsSizes[i];
      j += sameConfColorsCount[i];
    }
  }

  public final int getSize() {
    return size;
  }
}

package net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh;

import org.apache.commons.math3.util.CombinatoricsUtils;

public final class Ranks13BitsCommon {
  static final int numberOfRanks = 13;
  static final int numberOfRanksCombinations = 0x1 << numberOfRanks;
  static final int[] numberOfSetBits = new int[numberOfRanksCombinations];
  static final int[] msbIndexes = new int[numberOfRanksCombinations];
  static final int[] msbMasks = new int[numberOfRanksCombinations];
  static final int[] lsbMasks = new int[numberOfRanksCombinations];
  static final int[][] combinations = new int[numberOfRanks + 1][numberOfRanks + 1];
  static {
    for (int i = 0; i < numberOfRanksCombinations; i++) {
      numberOfSetBits[i] = computeNumberOfBits(i);
      msbIndexes[i] = computeMSB(i);
      msbMasks[i] = 0x1 << msbIndexes[i];
    }
    for (int i = 0; i < numberOfRanks + 1; i++) {
      for (int j = 0; j < numberOfRanks + 1; j++) {
        combinations[i][j] = comb(i, j);
      }
    }
  }

  private static final int comb(int n, int k) {
    if (k > n) {
      return 0;
    }
    return (int) CombinatoricsUtils.binomialCoefficient(n, k);
  }

  private static final int computeNumberOfBits(int set) {
    int res = 0;
    for (int i = 0; i < numberOfRanks; i++) {
      if ((set & (0x1 << i)) != 0) {
        res++;
      }
    }
    return res;
  }

  private static final int computeMSB(int set) {
    for (int i = numberOfRanks - 1; i >= 0; i--) {
      if ((set & (0x1 << i)) != 0) {
        return i;
      }
    }
    return -1;
  }

}

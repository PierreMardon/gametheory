package net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh;

import static net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.Ranks13BitsCommon.combinations;
import static net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.Ranks13BitsCommon.msbIndexes;
import static net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.Ranks13BitsCommon.msbMasks;
import static net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.Ranks13BitsCommon.numberOfRanks;
import static net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.Ranks13BitsCommon.numberOfSetBits;

public class PokerRanksGroupsIndexing {

  public static final int indexGroups(final int[] groupsSets) {
    int idx = 0;
    int ranksUsed = 0;
    int set;
    int idxMult = 1;
    int setIdx;
    int nbRanks;
    int msbMask;
    int j;
    for (int groupsSet : groupsSets) {
      set = groupsSet;
      setIdx = 0;
      nbRanks = numberOfSetBits[set];
      for (j = 0; j < nbRanks; j++) {
        msbMask = msbMasks[set];
        setIdx +=
            combinations[msbIndexes[set] - numberOfSetBits[(msbMask - 1) & ranksUsed]][nbRanks - j]; // +
                                                                                                     // 1
                                                                                                     // ?
        set ^= msbMask;
      }
      idx += setIdx * idxMult;
      idxMult *= combinations[numberOfRanks - numberOfSetBits[ranksUsed]][nbRanks];
      ranksUsed |= groupsSet;
    }
    return idx;
  }

  public static final void unindexGroup(final int idx, final int[] setsSizes, final int[] dest) {
    int nextIdx = idx;
    int setRanks;
    int ranksUsed = 0;
    int size;
    int set;
    int msbMask;
    int origMask;
    for (int i = 0; i < setsSizes.length; i++) {
      set = 0;
      size = combinations[numberOfRanks - numberOfSetBits[ranksUsed]][setsSizes[i]];
      setRanks = unindexSet(nextIdx % size, setsSizes[i]);
      nextIdx = nextIdx / size;
      // setRanks contains ranks that can have been shifted down
      // After this loop, set should contain the unshifted set
      while (setRanks != 0) {
        origMask = msbMask = msbMasks[setRanks];
        while (origMask << numberOfSetBits[((msbMask | (msbMask - 1)) & ranksUsed)] != msbMask) {
          msbMask = msbMask << 1;
        }
        set |= msbMask;
        setRanks ^= origMask;
      }
      ranksUsed |= set;
      dest[i] = set;
    }
  }

  public static final void unindexGroupLong(final long idx, final int[] setsSizes,
      final int[] dest) {
    long nextIdx = idx;
    int setRanks;
    int ranksUsed = 0;
    int size;
    int set;
    int msbMask;
    int origMask;
    for (int i = 0; i < setsSizes.length; i++) {
      set = 0;
      size = combinations[numberOfRanks - numberOfSetBits[ranksUsed]][setsSizes[i]];
      setRanks = unindexSetLong(nextIdx % size, setsSizes[i]);
      nextIdx = nextIdx / size;
      // setRanks contains ranks that can have been shifted down
      // After this loop, set should contain the unshifted set
      while (setRanks != 0) {
        origMask = msbMask = msbMasks[setRanks];
        while (origMask << numberOfSetBits[((msbMask | (msbMask - 1)) & ranksUsed)] != msbMask) {
          msbMask = msbMask << 1;
        }
        set |= msbMask;
        setRanks ^= origMask;
      }
      ranksUsed |= set;
      dest[i] = set;
    }
  }

  public static final int unindexSet(int idx, int setSize) {
    int maxRank = numberOfRanks - 1;
    int newIdx = idx;
    int res = 0;
    for (int newSetSize = setSize; newSetSize > 0; newSetSize--) {
      for (; combinations[maxRank][newSetSize] > newIdx; maxRank--) {
        ;
      }
      newIdx -= combinations[maxRank][newSetSize];
      res |= 0x1 << (maxRank--);
    }
    return res;
  }

  public static final int unindexSetLong(long idx, int setSize) {
    int maxRank = numberOfRanks - 1;
    long newIdx = idx;
    int res = 0;
    for (int newSetSize = setSize; newSetSize > 0; newSetSize--) {
      for (; combinations[maxRank][newSetSize] > newIdx; maxRank--) {
        ;
      }
      newIdx -= combinations[maxRank][newSetSize];
      res |= 0x1 << (maxRank--);
    }
    return res;
  }

  public static final int sizeForGroups(final int[] groupsSizes) {
    int res = 1;
    int sum = 0;
    for (int groupsSize : groupsSizes) {
      res *= combinations[numberOfRanks - sum][groupsSize];
      sum += groupsSize;
    }
    return res;
  }
}

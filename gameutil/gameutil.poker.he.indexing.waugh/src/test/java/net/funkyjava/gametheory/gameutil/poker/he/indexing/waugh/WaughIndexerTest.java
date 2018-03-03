package net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaughIndexerTest {

  @Test
  public void testFullUnindexIndex22() {
    WaughIndexer indexer = new WaughIndexer(new int[] {2, 2});
    boolean[] checked = new boolean[indexer.getIndexSize()];
    long[] toIndex = new long[2];
    long[] res = new long[2];
    List<Integer> perm = new ArrayList<>();
    perm.add(0);
    perm.add(1);
    perm.add(2);
    perm.add(3);
    for (int c1 = 0; c1 < 52; c1++) {
      for (int c2 = c1 + 1; c2 < 52; c2++) {
        for (int c3 = 0; c3 < 52; c3++) {
          if (c3 != c1 && c3 != c2) {
            for (int c4 = c3 + 1; c4 < 52; c4++) {
              if (c4 != c2 && c4 != c1) {
                toIndex[0] = 0x1l << ((c1 % 13) + 16 * (c1 / 13));
                toIndex[0] |= 0x1l << ((c2 % 13) + 16 * (c2 / 13));
                toIndex[1] = 0x1l << ((c3 % 13) + 16 * (c3 / 13));
                toIndex[1] |= 0x1l << ((c4 % 13) + 16 * (c4 / 13));
                int idx = indexer.index(toIndex);
                checked[idx] = true;
                indexer.unindex(idx, res);
                assertTrue("Indexer's index/unindex doesn't return an equivalent set for cards "
                    + c1 + " " + c2 + " " + c3 + " " + c4, areEquivalent(toIndex, res, perm));
              }
            }
          }
        }
      }
    }
    for (int i = 0; i < indexer.getIndexSize(); i++) {
      assertTrue("Index " + i + " wasn't reached !!", checked[i]);
    }
    log.info("Fully validated WaughIndexer for 2-2 cards groups index/unindex");
  }

  @Test
  public void testFullIndex23Speed() {
    WaughIndexer indexer = new WaughIndexer(new int[] {2, 3});
    long[] toIndex = new long[2];
    int nb = 0;

    int c1 = 0, c2, c3, c4, c5;
    long start = System.currentTimeMillis();
    for (; c1 < 52; c1++) {
      for (c2 = c1 + 1; c2 < 52; c2++) {
        for (c3 = 0; c3 < 52; c3++) {
          if (c3 != c1 && c3 != c2) {
            for (c4 = c3 + 1; c4 < 52; c4++) {
              if (c4 != c2 && c4 != c1) {
                for (c5 = c4 + 1; c5 < 52; c5++) {
                  if (c5 != c1 && c5 != c2) {
                    nb++;
                    toIndex[0] = 0x1l << ((c1 % 13) + 16 * (c1 / 13));
                    toIndex[0] |= 0x1l << ((c2 % 13) + 16 * (c2 / 13));
                    toIndex[1] = 0x1l << ((c3 % 13) + 16 * (c3 / 13));
                    toIndex[1] |= 0x1l << ((c4 % 13) + 16 * (c4 / 13));
                    toIndex[1] |= 0x1l << ((c5 % 13) + 16 * (c5 / 13));
                    indexer.index(toIndex);
                  }
                }
              }
            }
          }
        }
      }
    }
    log.info(
        "WaughIndexer indexed every possible flop ({} 2-3 cards groups) at the speed of {} indexings/s",
        nb, 1000 * ((double) nb) / (System.currentTimeMillis() - start));
  }

  @Test
  public void testFullIndex23AllPermsSpeed() {
    WaughIndexer indexer = new WaughIndexer(new int[] {2, 3});
    long[] toIndex = new long[2];
    int nb = 0;

    int c1 = 0, c2, c3, c4, c5;
    long start = System.currentTimeMillis();
    for (; c1 < 52; c1++) {
      for (c2 = 0; c2 < 52; c2++) {
        if (c2 != c1) {
          for (c3 = 0; c3 < 52; c3++) {
            if (c3 != c1 && c3 != c2) {
              for (c4 = 0; c4 < 52; c4++) {
                if (c4 != c3 && c4 != c2 && c4 != c1) {
                  for (c5 = 0; c5 < 52; c5++) {
                    if (c5 != c4 && c5 != c3 && c5 != c1 && c5 != c2) {
                      nb++;
                      toIndex[0] = 0x1l << ((c1 % 13) + 16 * (c1 / 13));
                      toIndex[0] |= 0x1l << ((c2 % 13) + 16 * (c2 / 13));
                      toIndex[1] = 0x1l << ((c3 % 13) + 16 * (c3 / 13));
                      toIndex[1] |= 0x1l << ((c4 % 13) + 16 * (c4 / 13));
                      toIndex[1] |= 0x1l << ((c5 % 13) + 16 * (c5 / 13));
                      indexer.index(toIndex);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    long time = System.currentTimeMillis() - start;
    log.info(
        "WaughIndexer indexed every possible flop with all permutations ({} 2-3 cards groups) at the speed of {} indexings/s, took {} s",
        nb, 1000 * ((double) nb) / ((time)), time / 1000);
  }

  private static boolean areEquivalent(long[] src, long[] res, List<Integer> perm) {
    if (src[0] == res[0] && src[1] == res[1]) {
      return true;
    }
    int[][] orig = perGroupColorsRanks(src);
    int[][] fin = perGroupColorsRanks(res);
    return permute(perm, 0, orig, fin);
  }

  private static boolean permute(java.util.List<Integer> arr, int k, int[][] src, int[][] res) {
    for (int i = k; i < arr.size(); i++) {
      java.util.Collections.swap(arr, i, k);
      if (permute(arr, k + 1, src, res)) {
        return true;
      }
      java.util.Collections.swap(arr, k, i);
    }
    if (k == arr.size() - 1) {
      return src[0][arr.get(0)] == res[0][0] && src[0][arr.get(1)] == res[0][1]
          && src[0][arr.get(2)] == res[0][2] && src[0][arr.get(3)] == res[0][3]
          && src[1][arr.get(0)] == res[1][0] && src[1][arr.get(1)] == res[1][1]
          && src[1][arr.get(2)] == res[1][2] && src[1][arr.get(3)] == res[1][3];
    }
    return false;
  }

  public static final int[][] perGroupColorsRanks(long[] groups) {
    int[][] res = new int[groups.length][];
    for (int i = 0; i < groups.length; i++) {
      res[i] = fromLongColorsRanks(groups[i]);
    }
    return res;
  }

  public static final int[] fromLongColorsRanks(long src) {
    return new int[] {(int) (src & 0xFFFF), (int) ((src >> 16) & 0xFFFF),
        (int) ((src >> 32) & 0xFFFF), (int) ((src >> 48) & 0xFFFF)};
  }
}

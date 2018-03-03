package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlopIndexerTest {
  @Test
  public void testFlopPerf() {
    final FlopIndexer indexer = new FlopIndexer();
    indexer.initialize();
    int nb = 0;

    int c1 = 0, c2, c3, c4, c5;
    long start = System.currentTimeMillis();
    final int[][] toIndex = new int[2][];
    final int[] hand = toIndex[0] = new int[2];
    final int[] board = toIndex[1] = new int[3];

    for (; c1 < 52; c1++) {
      hand[0] = c1;
      for (c2 = 0; c2 < 52; c2++) {
        if (c2 != c1) {
          hand[1] = c2;
          for (c3 = 0; c3 < 52; c3++) {
            if (c3 != c1 && c3 != c2) {
              board[0] = c3;
              for (c4 = 0; c4 < 52; c4++) {
                if (c4 != c3 && c4 != c2 && c4 != c1) {
                  board[1] = c4;
                  for (c5 = 0; c5 < 52; c5++) {
                    if (c5 != c1 && c5 != c2 && c5 != c3 && c5 != c4) {
                      board[2] = c5;
                      nb++;
                      indexer.indexOf(toIndex);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    final long time = System.currentTimeMillis() - start;
    log.info(
        "Djhemlig flop indexer indexed every possible flop with all permutations ({} 2-3 cards groups) at the speed of {} indexings/s, took {} s",
        nb, 1000 * ((double) nb) / ((time)), time / 1000);
  }
}

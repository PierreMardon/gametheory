package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

@Slf4j
public class ThreePlayersPreflopEquityTablesTest {

  static final Path path = Paths.get("/Users/pitt/PokerData/3PlayersFixed.dat");

  private static final boolean testWrite = false;
  private static final boolean testRead = false;

  @Test
  public void testWriteRead() throws IOException, InterruptedException, ClassNotFoundException {
    if (testWrite) {
      try (final FileOutputStream fos = new FileOutputStream(path.toFile())) {
        final ThreePlayersPreflopEquityTable tables = new ThreePlayersPreflopEquityTable();
        tables.compute();
        tables.write(fos);
      }
    }
    if (testRead) {
      final ThreePlayersPreflopEquityTable tables = new ThreePlayersPreflopEquityTable();
      try (final FileInputStream fis = new FileInputStream(path.toFile())) {
        tables.fill(fis);
      }
      final WaughIndexer indexer = new WaughIndexer(new int[] {2, 2, 2});
      final WaughIndexer holeIndexer = new WaughIndexer(new int[] {2});
      final int[][] cards = new int[3][2];
      final Cards52Strings str = new Cards52Strings(indexer.getCardsSpec());
      cards[0][0] = str.getCard("Ah");
      cards[0][1] = str.getCard("Ac");
      cards[1][0] = str.getCard("Kh");
      cards[1][1] = str.getCard("Qc");
      cards[2][0] = str.getCard("6d");
      cards[2][1] = str.getCard("5d");
      final int[][] hole1 = new int[][] {cards[0]};
      final int[][] hole2 = new int[][] {cards[1]};
      final int[][] hole3 = new int[][] {cards[2]};
      final int h1 = holeIndexer.indexOf(hole1);
      final int h2 = holeIndexer.indexOf(hole2);
      final int h3 = holeIndexer.indexOf(hole3);
      int[][] orderedCards = new int[3][];
      if (h1 <= h2 && h2 <= h3) {
        orderedCards = cards;
      } else if (h1 <= h3 && h3 <= h2) {
        orderedCards[0] = hole1[0];
        orderedCards[1] = hole3[0];
        orderedCards[2] = hole2[0];
      } else if (h2 <= h1 && h1 <= h3) {
        orderedCards[0] = hole2[0];
        orderedCards[1] = hole1[0];
        orderedCards[2] = hole3[0];
      } else if (h2 <= h3 && h3 <= h1) {
        orderedCards[0] = hole2[0];
        orderedCards[1] = hole3[0];
        orderedCards[2] = hole1[0];
      } else if (h3 <= h1 && h1 <= h2) {
        orderedCards[0] = hole3[0];
        orderedCards[1] = hole1[0];
        orderedCards[2] = hole2[0];
      } else if (h3 <= h2 && h2 <= h1) {
        orderedCards[0] = hole3[0];
        orderedCards[1] = hole2[0];
        orderedCards[2] = hole1[0];
      }
      final double[][] equities = tables.getEquities()[indexer.indexOf(orderedCards)];
      log.info("Equities for {} : {}", str.getStr(orderedCards), equities);
    }
  }

}

package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

@Slf4j
public class ThreePlayersReducedEquityTableTest {

  static final Path path = Paths.get("/Users/pitt/PokerData/3PlayersReduced.dat");

  private static final boolean testWrite = false;
  private static final boolean testRead = false;

  @Test
  public void testWriteRead() throws IOException, InterruptedException, ClassNotFoundException {
    if (testWrite) {
      // final ThreePlayersPreflopReducedEquityTable tables = new
      // ThreePlayersPreflopReducedEquityTable();
      // tables.compute();
      // try (final FileOutputStream fos = new
      // FileOutputStream(path.toFile())) {
      // tables.write(fos);
      // }
    }
    if (testRead) {
      final ThreePlayersPreflopReducedEquityTable tables =
          new ThreePlayersPreflopReducedEquityTable();
      try (final FileInputStream fis = new FileInputStream(path.toFile())) {
        tables.fill(fis);
      }
      final WaughIndexer holeIndexer = new WaughIndexer(new int[] {2});
      final int[][] cards = new int[3][2];
      final Cards52Strings str = new Cards52Strings(holeIndexer.getCardsSpec());
      cards[2][0] = str.getCard("Ah");
      cards[2][1] = str.getCard("Ac");
      cards[1][0] = str.getCard("Kh");
      cards[1][1] = str.getCard("Qc");
      cards[0][0] = str.getCard("6d");
      cards[0][1] = str.getCard("5d");
      final int[][] hole1 = new int[][] {cards[0]};
      final int[][] hole2 = new int[][] {cards[1]};
      final int[][] hole3 = new int[][] {cards[2]};
      final int h1 = holeIndexer.indexOf(hole1);
      final int h2 = holeIndexer.indexOf(hole2);
      final int h3 = holeIndexer.indexOf(hole3);
      final double[][] equities = tables.getReducedEquities()[h1][h2][h3];
      log.info("Equities for {} : {}", str.getStr(cards), equities);
    }
  }

}

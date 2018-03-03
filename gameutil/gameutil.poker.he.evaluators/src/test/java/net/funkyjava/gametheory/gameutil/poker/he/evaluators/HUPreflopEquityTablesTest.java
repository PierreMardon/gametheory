package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;

@Slf4j
public class HUPreflopEquityTablesTest {

  static final Path path = Paths.get("/Users/pitt/HE_HU_EQUITY.dat");

  private static final boolean testWrite = false;
  private static final boolean testRead = false;

  @Test
  public void testWrite() throws IOException, InterruptedException {
    if (!testWrite) {
      return;
    }
    try (final ObjectOutputStream oos =
        new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
      final HUPreflopEquityTables tables = new HUPreflopEquityTables();
      tables.compute();
      oos.writeObject(tables);
    }
  }

  @Test
  public void testRead() throws FileNotFoundException, IOException, ClassNotFoundException {
    if (!testRead) {
      return;
    }
    try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
      final HUPreflopEquityTables tables = (HUPreflopEquityTables) ois.readObject();
      String c1 = "Ad";
      String c2 = "Ac";
      String c3 = "Ah";
      String c4 = "Kc";
      Cards52Strings cString = new Cards52Strings(tables.getCardsSpec());
      final int[] heroCards = {cString.getCard(c1), cString.getCard(c2)};
      final int[] vilainCards = {cString.getCard(c3), cString.getCard(c4)};

      final double equity = tables.getEquity(heroCards, vilainCards);
      log.info("Equity for {}{} vs {}{} is {}", c1, c2, c3, c4, equity);

      final double reducedEquity = tables.getReducedEquity(heroCards, vilainCards);
      log.info("Reduced equity for {}{} vs {}{} (= AA vs AKo) is {}", c1, c2, c3, c4,
          reducedEquity);
    }
  }
}

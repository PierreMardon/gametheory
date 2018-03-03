package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

@Slf4j
public class AllHoldemHSTablesTest {

  private static boolean runLongTest = false;
  private AllHoldemHSTables<WaughIndexer, WaughIndexer, WaughIndexer, WaughIndexer> all;

  /**
   * Test values based on <a href=
   * "http://www.poker-ai.org/archive/pokerai.org/pf3/viewtopic57d8.html?f=3&t=2764&view=next"
   * >Indiana results on poker-ai</a> , with big tolerance (1.5E-3) because its implementation has a
   * bias (euclidian division for ties/2) <br>
   * <a href=
   * "http://www.poker-ai.org/archive/www.pokerai.org/pf3/viewtopic1e13-2.html?f=3&t=444&st=0&sk=t&sd=a&start=20"
   * >Indiana base implementation</a>
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  public void testValues() throws IOException, URISyntaxException {
    if (!runLongTest) {
      log.info("Not running AllHoldemHSTables testValues (takes too long). "
          + "Set this test class runLongTest boolean to true to execute it.");
      return;
    }
    log.info("Testing all EHS tables");
    final IntCardsSpec specs = DefaultIntCardsSpecs.getDefault();
    Cards52Strings c = new Cards52Strings(specs);
    log.info("Loading all EHS tables");
    all = AllHoldemHSTables.getTablesWithWaughIndexersTwoPlusTwoEval();
    all.compute();
    final double[] flopEhsEval = all.getFlopEHSTable();
    final double[] flopEhs2Eval = all.getFlopEHS2Table();
    testEHSEHS2(0.853, 0.733, "AsAd", "4cJh9s", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(0.829, 0.697, "AhJd", "4cJh9s", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(0.927, 0.864, "AhJd", "AcJh9s", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(0.952, 0.911, "JdJc", "4cJh9s", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(0.94, 0.888, "AhJd", "JcJh9s", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(0.951, 0.909, "KhQs", "9sTcJh", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(0.982, 0.967, "AhJh", "9hTh4h", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(0.994, 0.989, "AhJd", "JcJhAs", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(1.0, 1.0, "JhJd", "JcJs4s", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(1.0, 1.0, "KsQs", "9sTsJs", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(0.709, 0.623, "JsTs", "9s8s3d", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(0.553, 0.439, "JsTs", "9c8h3d", c, flopEhsEval, flopEhs2Eval);
    testEHSEHS2(0.716, 0.584, "AsQs", "Js8s3d", c, flopEhsEval, flopEhs2Eval);
    double[] preflopEhsEval = all.getPreflopEHSTable();
    testEHS(0.85204, "AsAd", c, preflopEhsEval);
    testEHS(0.40512, "7h5s", c, preflopEhsEval);
    testEHS(0.35984, "2c3c", c, preflopEhsEval);
  }

  private void testEHSEHS2(double ehsValue, double ehs2Value, String handStr, String flopStr,
      Cards52Strings c, double[] ehs, double[] ehs2) {
    int[][] flop = {c.getCards(handStr), c.getCards(flopStr)};
    double cEhs = ehs[all.getFlopCardsIndexer().indexOf(flop)];
    double cEhs2 = ehs2[all.getFlopCardsIndexer().indexOf(flop)];
    assertTrue("For hand " + handStr + " flop " + flopStr + " expected EHS = " + ehsValue
        + " but got " + cEhs, Math.abs(cEhs - ehsValue) < 1.5E-3);
    assertTrue("For hand " + handStr + " flop " + flopStr + " expected EHS2 = " + ehs2Value
        + " but got " + cEhs2, Math.abs(cEhs2 - ehs2Value) < 1.5E-3);
  }

  private void testEHS(double ehsValue, String handStr, Cards52Strings c, double[] ehs) {
    int[][] flop = {c.getCards(handStr)};
    double cEhs = ehs[all.getHoleCardsIndexer().indexOf(flop)];
    assertTrue("For hand " + handStr + " expected EHS = " + ehsValue + " but got " + cEhs,
        Math.abs(cEhs - ehsValue) < 1.E-4);
  }
}

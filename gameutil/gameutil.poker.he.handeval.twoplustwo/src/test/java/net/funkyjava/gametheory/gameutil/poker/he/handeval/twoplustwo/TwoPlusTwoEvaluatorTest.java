package net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo;

import org.junit.BeforeClass;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;

/**
 * Test class for {@link TwoPlusTwoEvaluator}
 *
 * @author Pierre Mardon
 *
 */
@Slf4j
public class TwoPlusTwoEvaluatorTest {

  private static TwoPlusTwoEvaluator eval;
  private static Cards52Strings str;

  /**
   * Generate tables
   */
  @BeforeClass
  public static void generateTables() {
    log.info("Generating tables");
    eval = new TwoPlusTwoEvaluator();
    str = new Cards52Strings(eval.getCardsSpec());
  }

  /**
   * Not really a test, look at output to check if works as expected
   *
   * @throws Exception
   */
  @Test
  public void testCompare7CardsHands() throws Exception {
    log.info("Samples of hands comparison : ");
    final int[] allCards = new int[9];
    final int[] h1 = new int[2];
    final int[] h2 = new int[2];
    final int[] board = new int[5];
    Deck52Cards deck = new Deck52Cards(1);
    int res;
    for (int i = 0; i < 50; i++) {
      deck.reset();
      deck.draw(allCards);
      System.arraycopy(allCards, 0, h1, 0, 2);
      System.arraycopy(allCards, 2, h2, 0, 2);
      System.arraycopy(allCards, 4, board, 0, 5);
      res = 0;
      res = eval.compare7CardsHands(h1, h2, board);
      log.debug("{} {} {} | {}", toString(h1), res > 0 ? '>' : res < 0 ? '<' : '=', toString(h2),
          toString(board));
    }
  }

  /**
   * Just print hands strings
   */
  @Test
  public void testHandsStr() {
    log.info("Hands to string : ");
    for (int i = 1; i < 53; i++) {
      log.info("{} : {}", i, str.getStr(i));
    }
  }

  private static String toString(int[] cards) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < cards.length; i++) {
      sb.append(str.getStr(cards[i]));
      if (i < cards.length - 1) {
        sb.append(' ');
      }
    }
    return sb.toString();

  }

}

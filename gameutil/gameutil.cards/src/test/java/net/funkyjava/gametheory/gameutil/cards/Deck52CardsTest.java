package net.funkyjava.gametheory.gameutil.cards;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Pierre Mardon
 *
 */
@Slf4j
public class Deck52CardsTest {

  /**
   * Test one shot draws
   *
   * @throws Exception unexpected exception
   */
  @Test
  public void testOneShotDeckDraw() throws Exception {
    Deck52Cards d = new Deck52Cards(1);
    int[] fullDeck = new int[52];
    d.oneShotDeckDraw(fullDeck);
    log.info("Full deck drawing : {}", fullDeck);
    mainloop: for (int i = 1; i < 53; i++) {
      for (int j = 0; j < 52; j++) {
        if (fullDeck[j] == i) {
          continue mainloop;
        }
      }
      fail("The card " + i + " wasn't found");
    }
    assertTrue("The size of the deck should be 52", d.getSize() == 52);
    int[] heHUCards = new int[9];
    long start = System.currentTimeMillis();
    int nbDrawing = 1000000;
    for (int i = 0; i < nbDrawing; i++) {
      d.oneShotDeckDraw(heHUCards);
    }
    double val;
    log.info("{} Full Hold'em Heads Up drawings in {}ms, {} drawings per second", nbDrawing,
        val = (System.currentTimeMillis() - start), nbDrawing * 1000 / val);
  }

  /**
   * @throws Exception unexpected exception
   */
  @Test
  public void testDraw() throws Exception {
    Deck52Cards d = new Deck52Cards(1);
    int[] fullDeck = new int[52];
    d.draw(fullDeck);
    log.info("Full deck drawing : {}", fullDeck);
    mainloop: for (int i = 1; i < 53; i++) {
      for (int j = 0; j < 52; j++) {
        if (fullDeck[j] == i) {
          continue mainloop;
        }
      }
      fail("The card " + i + " wasn't found");
    }
    assertTrue("The deck should be empty", d.getSize() == 0);
    int[] heHUCards = new int[9];
    int nbDrawing = 1000000;
    long start = System.currentTimeMillis();
    for (int i = 0; i < nbDrawing; i++) {
      d.reset();
      d.draw(heHUCards);
    }
    double val;
    log.info("{} Full Hold'em Heads Up drawings in {}ms, {} drawings per second", nbDrawing,
        val = (System.currentTimeMillis() - start), nbDrawing * 1000 / val);
  }

  /**
   *
   * @throws Exception
   */
  @Test
  public void testDrawAllGroupsCombinations() throws Exception {
    log.info("Verifying that drawing all combinations of  2 x 2 cards works well");
    final boolean[][] created = new boolean[52 * 52][52 * 52];
    new Deck52Cards().drawAllGroupsCombinations(new int[] {2, 2}, new CardsGroupsDrawingTask() {
      int g1;
      int g2;

      @Override
      public boolean doTask(int[][] cardsGroups) {
        g1 = Math.min(cardsGroups[0][0], cardsGroups[0][1])
            + 52 * Math.max(cardsGroups[0][0], cardsGroups[0][1]);
        g2 = Math.min(cardsGroups[1][0], cardsGroups[1][1])
            + 52 * Math.max(cardsGroups[1][0], cardsGroups[1][1]);
        assertTrue("This draw was already created : " + Arrays.deepToString(cardsGroups),
            !created[g1][g2]);
        created[g1][g2] = true;
        return true;
      }
    });
    for (int c1 = 0; c1 < 52; c1++) {
      for (int c2 = c1 + 1; c2 < 52; c2++) {
        int g1 = c1 + 52 * c2;
        for (int c3 = 0; c3 < 52; c3++) {
          if (c3 == c1 || c3 == c2) {
            continue;
          }
          for (int c4 = c3 + 1; c4 < 52; c4++) {
            if (c4 == c1 || c4 == c2) {
              continue;
            }
            assertTrue("This combination was not created " + c1 + " " + c2 + " " + c3 + " " + c4,
                created[g1][c3 + 52 * c4]);
          }
        }
      }
    }

  }

  private static class CountingDrawingTask implements CardsGroupsDrawingTask {
    @Getter
    private long count = 0;

    @Override
    public boolean doTask(int[][] cardsGroups) {
      count++;
      return true;
    }

  }

  /**
   *
   * @throws Exception
   */
  @Test
  public void testDrawRiver() throws Exception {
    log.info(
        "Verifying how long it takes to draw all 2x5 cards groups (like NLHE hole cards + flop/turn/river)");
    long start = System.currentTimeMillis();
    CountingDrawingTask task = new CountingDrawingTask();
    new Deck52Cards().drawAllGroupsCombinations(new int[] {2, 5}, task);
    log.info("Took {} ms, draws count : {}", System.currentTimeMillis() - start, task.getCount());
  }
}

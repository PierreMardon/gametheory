/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.he.handeval;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;

/**
 * Evaluates five cards hands for holdem. The evaluator gives its cards representation by providing
 * a {@link IntCardsSpec}. Thread safety is not required.
 *
 * @author Pierre Mardon
 *
 */
public interface Holdem5CardsEvaluator {

  /**
   * Compare two players hold'em hands
   *
   * @param h1 the first player's two hole cards
   * @param h2 the second player's two hole cards
   * @param board the board's five cards
   * @return > 0 when first player wins, < 0 when second player wins, and 0 on equality
   */
  int compare5CardsHands(int[] h1, int[] h2, int[] board);

  /**
   * Gets the evaluation of a 5 cards hand
   *
   * @param hand
   * @return the strength of the 5 cards hand
   */
  int get5CardsEval(int[] hand);

  /**
   * Write all evaluations of the array of two hole cards hands for a same board
   *
   * @param hands the hole cards
   * @param board the board's five cards
   * @param dest the evaluations destination array
   */
  void get5CardsEvals(int[][] hands, int[] board, int[] dest);

  /**
   * Gets the int cards specifications
   *
   * @return the int cards specifications
   */
  IntCardsSpec getCardsSpec();
}

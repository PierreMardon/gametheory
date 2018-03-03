package net.funkyjava.gametheory.gameutil.cards;

/**
 * Simple task to run over a cards drawing
 *
 * @author Pierre Mardon
 *
 */
public interface CardsGroupsDrawingTask {
  /**
   * The task method
   *
   * @param cardsGroups the grouped cards of a draw
   * @return continue drawing boolean
   */
  boolean doTask(int[][] cardsGroups);
}

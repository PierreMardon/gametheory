package net.funkyjava.gametheory.gameutil.cards.indexing;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;

/**
 * Interface intending to generalize the indexing task of poker needed to distinguish and group
 * together equivalent cards related information sets.<br>
 * For strictly equivalent available information, two cards groups sets must return the same
 * index.<br>
 * A cards groups set is an ordered set of not-ordered cards groups.<br>
 * For example in no-limit hold'em poker, the cards information set on flop street for a player is a
 * group of two cards (his hole cards) and a group of three other cards (the flop).<br>
 * There are many ways an indexer can behave depending on the implementation. It can be perfect with
 * no gaps, or imperfect : can not distinguish two equivalent situations, or grouping not equivalent
 * situations (as for bucketing), or leaving gaps, meaning that the ensemble of indexes is not
 * compact.<br>
 * Also, one for NLHE poker may not fit other card games. For now, real games identification is not
 * abstracted.<br>
 * We will check this by using a String id. We may change this system to find a more conventional
 * way.
 *
 *
 * @author Pierre Mardon
 *
 */
public interface CardsGroupsIndexer {

  /**
   * Get the index of a cards groups set
   *
   * @param cardsGroups the cards groups set
   * @return the index
   */
  public int indexOf(int[][] cardsGroups);

  /**
   * Retrieve the indexes ensemble size. There can be gaps.
   *
   * @return the indexes ensemble size
   */
  public int getIndexSize();

  /**
   * Gets the int cards specifications
   *
   * @return the int cards specifications
   */
  IntCardsSpec getCardsSpec();

  /**
   * Check if this indexer can index for those groups sizes specifications.
   *
   * @param groupsSizes sizes of the cards groups to be indexed
   * @return compatibility boolean
   */
  public boolean canHandleGroups(int[] groupsSizes);

  /**
   * Check if this indexer is compatible with a game
   *
   * @param gameId the string representation of the game
   *
   * @return compatibility boolean
   */
  public boolean isCompatible(String gameId);
}

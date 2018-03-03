package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;

/**
 * Cards specification for Djhemlig indexers
 *
 * @author Pierre Mardon
 *
 */
public class DjhemligCardsSpec implements IntCardsSpec {

  @Override
  public int getOffset() {
    return 0;
  }

  @Override
  public int getStandardRank(int card) {
    return card / 4;
  }

  @Override
  public int getStandardColor(int card) {
    return card % 4;
  }

  @Override
  public boolean sameColor(int card1, int card2) {
    return card1 % 4 == card2 % 4;
  }

  @Override
  public boolean sameRank(int card1, int card2) {
    return card1 / 4 == card2 / 4;
  }

  @Override
  public int getCard(int stdRank, int stdColor) {
    return stdColor + 4 * stdRank;
  }

}

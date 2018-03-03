/**
 *
 */
package net.funkyjava.gametheory.gameutil.cards;

/**
 * This interface is a bridge from a particular integer cards implementation and an arbitrary
 * standard. The specifications define an offset with {@link #getOffset()} that indicates that valid
 * values will be between this offset (inclusive) and the offset + deckSize (exclusive).
 *
 * @author Pierre Mardon
 *
 */
public interface IntCardsSpec {
  /**
   * Gets the cards indexing offset
   *
   * @return the offset for cards indexing
   */
  int getOffset();

  /**
   * Gets the rank of a card with 0 = deuce, ..., 12 = ace for 52 cards decks, or 0 = seven , ..., 7
   * = ace for 32 cards deck
   *
   * @param card the int card, >= offset && < offset + deckSize
   * @return the standard rank of the card
   */
  int getStandardRank(int card);

  /**
   * Gets consistent "arbitrary" color of a card. In fact it could be not arbitrary as it can be
   * used to distribute odd chips when splitting pots. For that use only, we state that club = 0,
   * diamond = 1, heart = 2 and spade = 3, as exposed by {@link Cards52Strings#getColorStr(int)}
   *
   * @param card the int card, >= offset && < offset + deckSize
   * @return the standard color of the card
   */
  int getStandardColor(int card);

  /**
   * Checks if two cards have the same color
   *
   * @param card1 the first card
   * @param card2 the second card
   * @return true when the cards have the same color
   */
  boolean sameColor(int card1, int card2);

  /**
   * Checks if two cards have the same rank
   *
   * @param card1 the first card
   * @param card2 the second card
   * @return true when the cards have the same rank
   */
  boolean sameRank(int card1, int card2);

  /**
   * Get a card int representation based on his standard rank and color
   *
   * @param stdRank the card's standard rank between 0 and 12 included
   * @param stdColor the card's standard color between 0 and 3 included
   * @return the card int representation
   */
  int getCard(int stdRank, int stdColor);

}

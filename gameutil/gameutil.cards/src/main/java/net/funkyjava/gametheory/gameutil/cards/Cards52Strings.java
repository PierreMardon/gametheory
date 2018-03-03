/**
 *
 */
package net.funkyjava.gametheory.gameutil.cards;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.NonNull;

/**
 * This class and {@link IntCardsSpec} intend to standardize the string representation of int cards.
 *
 * @author Pierre Mardon
 *
 */
public class Cards52Strings {

  /**
   * The ranks strings
   */
  public static final String[] ranks =
      {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};
  /**
   * The colors strings
   */
  public static final String[] colors = {"c", "d", "h", "s"};

  /**
   * The cards specifications
   */
  private final IntCardsSpec spec;

  /**
   * @param spec The cards specifications
   *
   */
  public Cards52Strings(IntCardsSpec spec) {
    this.spec = spec;
  }

  /**
   * Gets the card's rank string.
   *
   * @param card the int card compliant with the spec
   * @return the rank string
   */
  public String getRankStr(int card) {
    return ranks[spec.getStandardRank(card)];
  }

  /**
   * Gets the card's color string.
   *
   * @param card the int card compliant with the spec
   * @return the color string
   */
  public String getColorStr(int card) {
    return colors[spec.getStandardColor(card)];
  }

  /**
   * Gets the card's string.
   *
   * @param card the int card compliant with the spec
   * @return the card's string
   */
  public String getStr(int card) {
    return ranks[spec.getStandardRank(card)] + colors[spec.getStandardColor(card)];
  }

  /**
   * Gets the cards string
   *
   * @param cards the int cards compliant with the spec
   * @return the cards string
   */
  public String getStr(int[] cards) {
    StringBuilder b = new StringBuilder();
    for (int card : cards) {
      b.append(ranks[spec.getStandardRank(card)] + colors[spec.getStandardColor(card)]);
    }
    return b.toString();
  }

  /**
   * Gets the cards groups string
   *
   * @param cards the int cards groups compliant with the spec
   * @return the cards string
   */
  public String getStr(int[][] cards) {
    StringBuilder b = new StringBuilder();
    for (int[] card2 : cards) {
      for (int card : card2) {
        b.append(ranks[spec.getStandardRank(card)] + colors[spec.getStandardColor(card)]);
      }
      b.append(' ');
    }
    return b.toString();
  }

  /**
   * Gets the int value of a String represented card
   *
   * @param cardStr String representation of the card : "2c", ..., "As"
   * @return the int value of the card for the specs
   */
  public int getCard(@NonNull String cardStr) {
    checkArgument(cardStr.length() == 2, "Wrong card string size");
    return spec.getCard(getStandardRank(cardStr.substring(0, 1)),
        getStandardColor(cardStr.substring(1, 2)));
  }

  /**
   * Return multiple cards as an int array. Cards must be represented with no space, according to
   * standard representation, eg : "AdAc2hJd"
   *
   * @param cardsStr String representation of the cards
   * @return the int representations of the cards for the specs as an array
   */
  public int[] getCards(String cardsStr) {
    checkArgument(cardsStr.length() % 2 == 0, "Wrong card string size, must be multiple of 2");
    final int[] result = new int[cardsStr.length() / 2];
    for (int i = 0; i < result.length; i++) {
      result[i] = getCard(cardsStr.substring(2 * i, 2 * i + 2));
    }
    return result;
  }

  /**
   * Get the standard rank of a rank String limited to one character
   *
   * @param rank String representation of the rank : "2", "3",..., "9", "T", "J", "Q", "K", "A"
   * @return the standard rank represented by the String
   */
  public static int getStandardRank(@NonNull String rank) {
    checkArgument(rank.length() == 1, "Cannot translate String rank of length != 1");
    for (int i = 0; i < ranks.length; i++) {
      if (ranks[i].equals(rank)) {
        return i;
      }
    }
    throw new IllegalArgumentException("Provided rank '" + rank + "' is not a valid one");
  }

  /**
   * Get the standard color of a color String limited to one character
   *
   * @param color String representation of the color : "c", "d", "h", "s"
   * @return the standard color represented by the String
   */
  public static int getStandardColor(@NonNull String color) {
    checkArgument(color.length() == 1, "Cannot translate String color of length != 1");
    for (int i = 0; i < colors.length; i++) {
      if (colors[i].equals(color)) {
        return i;
      }
    }
    throw new IllegalArgumentException("Provided color '" + color + "' is not a valid one");
  }

}

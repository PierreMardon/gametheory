package net.funkyjava.gametheory.gameutil.cards;

/**
 * Convenience class make modules with different {@link IntCardsSpec} communicate.
 *
 * @author Pierre Mardon
 *
 */
public class Cards52SpecTranslator {

  private final int srcOffset, destOffset;
  private final int[] conv = new int[52], reverse = new int[52];
  private final boolean isSame;

  /**
   * Constructor
   *
   * @param src the source cards specification for this translator
   * @param dest the destination cards specification for this translator
   */
  public Cards52SpecTranslator(IntCardsSpec src, IntCardsSpec dest) {
    srcOffset = src.getOffset();
    destOffset = dest.getOffset();
    if (isSame = areEquivalent(src, dest)) {
      return;
    }
    for (int i = 0; i < 52; i++) {
      conv[i] =
          dest.getCard(src.getStandardRank(i + srcOffset), src.getStandardColor(i + srcOffset));
      reverse[i] =
          src.getCard(dest.getStandardRank(i + destOffset), dest.getStandardColor(i + destOffset));
    }
  }

  /**
   * Get the destination value of a source card
   *
   * @param srcCard the source card
   * @return the destination card
   */
  public int translate(int srcCard) {
    if (isSame) {
      return srcCard;
    }
    return conv[srcCard - srcOffset];
  }

  /**
   * Get the source value of a destination card
   *
   * @param destCard the destination card
   * @return the source card
   */
  public int reverse(int destCard) {
    if (isSame) {
      return destCard;
    }
    return reverse[destCard - destOffset];
  }

  /**
   * Translate a groups of source cards set to destination values
   *
   * @param cardsGroups the source cards groups
   */
  public void translate(int[][] cardsGroups) {
    if (isSame) {
      return;
    }
    final int srcOffset = this.srcOffset;
    final int[] conv = this.conv;
    final int nbCardsGroups = cardsGroups.length;
    for (int i = 0; i < nbCardsGroups; i++) {
      final int[] group = cardsGroups[i];
      final int groupLength = group.length;
      for (int j = 0; j < groupLength; j++) {
        cardsGroups[i][j] = conv[cardsGroups[i][j] - srcOffset];
      }
    }
  }

  /**
   * Translate a groups of destination cards set to source values
   *
   * @param cardsGroups the destination cards groups
   */
  public void reverse(int[][] cardsGroups) {
    if (isSame) {
      return;
    }
    final int destOffset = this.destOffset;
    final int[] reverse = this.reverse;
    final int nbCardsGroups = cardsGroups.length;
    for (int i = 0; i < nbCardsGroups; i++) {
      final int[] group = cardsGroups[i];
      final int groupLength = group.length;
      for (int j = 0; j < groupLength; j++) {
        cardsGroups[i][j] = reverse[cardsGroups[i][j] - destOffset];
      }
    }
  }

  /**
   * Translate an array of source cards to destination values
   *
   * @param cards the source cards
   */
  public void translate(int[] cards) {
    if (isSame) {
      return;
    }
    final int srcOffset = this.srcOffset;
    final int[] conv = this.conv;
    final int nbCards = cards.length;
    for (int i = 0; i < nbCards; i++) {
      cards[i] = conv[cards[i] - srcOffset];
    }
  }

  /**
   * Translate an array of destination cards to source values
   *
   * @param cards the destination cards
   */
  public void reverse(int[] cards) {
    if (isSame) {
      return;
    }
    final int destOffset = this.destOffset;
    final int[] reverse = this.reverse;
    final int nbCards = cards.length;
    for (int i = 0; i < nbCards; i++) {
      cards[i] = reverse[cards[i] - destOffset];
    }
  }

  /**
   * Check if two cards specifications are equivalent
   *
   * @param specs1 the first cards specs
   * @param specs2 the second cards specs
   * @return true when the two specifications are equivalent
   */
  public static boolean areEquivalent(IntCardsSpec specs1, IntCardsSpec specs2) {
    if (specs1 == specs2) {
      return true;
    }
    if (specs1.getOffset() != specs2.getOffset()) {
      return false;
    }
    for (int i = specs1.getOffset(); i < specs1.getOffset() + 52; i++) {
      if (specs1.getStandardColor(i) != specs2.getStandardColor(i)
          || specs1.getStandardRank(i) != specs2.getStandardRank(i)) {
        return false;
      }
    }
    return true;
  }
}

package net.funkyjava.gametheory.gameutil.cards;

import java.io.Serializable;

/**
 *
 * The default int cards specifications
 *
 * @author Pierre Mardon
 */
public class DefaultIntCardsSpecs implements IntCardsSpec, Serializable {

  private static final long serialVersionUID = -4831459429122083580L;
  private final int offset;

  /**
   * Constructor for offset 0
   */
  public DefaultIntCardsSpecs() {
    this.offset = 0;
  }

  /**
   * Constructor for a given offset
   *
   * @param offset
   */
  public DefaultIntCardsSpecs(int offset) {
    this.offset = offset;
  }

  @Override
  public boolean sameRank(int card1, int card2) {
    return (card1 - offset) / 4 == (card2 - offset) / 4;
  }

  @Override
  public boolean sameColor(int card1, int card2) {
    return (card1 - offset) % 4 == (card2 - offset) % 4;
  }

  @Override
  public int getStandardRank(int card) {
    return (card - offset) / 4;
  }

  @Override
  public int getStandardColor(int card) {
    return (card - offset) % 4;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public int getCard(int stdRank, int stdColor) {
    return offset + 4 * stdRank + stdColor;
  }

  private static DefaultIntCardsSpecs defaultSpec = new DefaultIntCardsSpecs();

  /**
   * Get default cards specifications
   *
   * @return the default cards specifications
   */
  public static DefaultIntCardsSpecs getDefault() {
    return defaultSpec;
  }
}

/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a blind value a player is expected to pay
 *
 * @author Pierre Mardon
 *
 */
@AllArgsConstructor
public class BlindValue {

  /**
   * Type of the blind
   *
   * @author Pierre Mardon
   *
   */
  public static enum Type {
    /**
     * Small blind
     */
    SB,
    /**
     * Big blind
     */
    BB
  }

  @Getter
  private final Type type;

  @Getter
  private final int value;

}

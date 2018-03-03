/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A raise range
 *
 * @author Pierre Mardon
 *
 */
@Data
@AllArgsConstructor
public class RaiseRange {

  private final int oldBet;
  private final int min;
  private int max;

  /**
   * Check if this range exists
   *
   * @return true when this range exists
   */
  public boolean exists() {
    return min > 0 && max >= min && oldBet >= 0;
  }

  /**
   * Get the minimum chips count to add to perform a raise
   *
   * @return the minimum chips count to add to perform a raise
   */
  public int getMinToAdd() {
    return min - oldBet;
  }

  /**
   * Get the maximum chips count to add to perform a raise
   *
   * @return the maximum chips count to add to perform a raise
   */
  public int getMaxToAdd() {
    return max - oldBet;
  }

  /**
   * Get an invalid raise range
   *
   * @return an invalid raise range
   */
  public static RaiseRange getNoRange() {
    return new RaiseRange(-1, -1, -1);
  }

  /**
   * Get a singleton raise range
   *
   * @param bet current player's bet
   * @param singleValue the only raise value
   * @return the singleton raise range
   */
  public static RaiseRange getSingleton(int bet, int singleValue) {
    return new RaiseRange(bet, singleValue, singleValue);
  }

}

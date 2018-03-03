/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.moves;

/**
 * Enum for {@link Move} types.
 *
 * @author Pierre Mardon
 *
 */
public enum MoveType {
  /**
   * Call
   */
  CALL,
  /**
   * Bet
   */
  BET,
  /**
   * Raise
   */
  RAISE,
  /**
   * Fold
   *
   */
  FOLD,
  /**
   * Ante
   */
  ANTE,
  /**
   * Small blind
   */
  SB,
  /**
   * Big blind
   */
  BB,
  /**
   * Refuse to pay ante
   */
  NO_ANTE,
  /**
   * Refuse to pay blind
   */
  NO_BLIND
}

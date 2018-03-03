/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds;

/**
 * Enum for the state of a round
 *
 * @author Pierre Mardon
 *
 */
public enum RoundState {
  /**
   * Round is waiting for a round
   */
  WAITING_MOVE,
  /**
   * Round is ended. If there's no next round, there must be showdown.
   */
  NEXT_ROUND,
  /**
   * Round ended, showdown to resolve
   */
  SHOWDOWN,
  /**
   * All players folded but one
   */
  END_NO_SHOWDOWN,
  /**
   * The round was canceled
   */
  CANCELED
}

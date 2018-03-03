/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.moves;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * The move class
 *
 * @author Pierre Mardon
 *
 */
@EqualsAndHashCode
public class Move {

  public static final int FOLD_VALUE = -1;

  /**
   * The move's player
   */
  @Getter
  private int player;

  /**
   * The move's type
   */
  @Getter
  private MoveType type;

  /**
   * The move's value
   */
  @Getter
  private int value;

  /**
   * The previous bet of the player in this round. Meaningful only for call and raise
   */
  @Getter
  private int oldBet;

  private Move(int player, MoveType type, int value, int oldBet) {
    this.player = player;
    this.type = type;
    this.value = value;
    this.oldBet = oldBet;
  }

  /**
   * Get the chips added by the player to its current bet by this move
   *
   * @return the added chips
   */
  public int addedChips() {
    return value - oldBet;
  }

  /**
   * Get fold move
   *
   * @param player the move's player
   * @return the fold move for this player
   */
  public static Move getFold(int player) {
    return new Move(player, MoveType.FOLD, FOLD_VALUE, FOLD_VALUE);
  }

  /**
   * Get a bet move
   *
   * @param player the move's player
   * @param value the bet value
   * @return the bet move
   */
  public static Move getBet(int player, int value) {
    return new Move(player, MoveType.BET, value, 0);
  }

  /**
   * Get a call move
   *
   * @param player the move's player
   * @param value the call value
   * @param oldBet the bet value of the player before this call
   * @return the call move
   */
  public static Move getCall(int player, int value, int oldBet) {
    return new Move(player, MoveType.CALL, value, oldBet);
  }

  /**
   * Get a raise move
   *
   * @param player the move's player
   * @param value the raise value
   * @param oldBet the bet value of the player before this raise
   * @return the raise move
   */
  public static Move getRaise(int player, int value, int oldBet) {
    return new Move(player, MoveType.RAISE, value, oldBet);
  }

  /**
   * Get an ante move
   *
   * @param player the move's player
   * @param value the ante value
   * @return the ante move
   */
  public static Move getAnte(int player, int value) {
    return new Move(player, MoveType.ANTE, value, 0);
  }

  /**
   * Get an ante pay refuse move
   *
   * @param player the move's player
   * @return the no ante move
   */
  public static Move getNoAnte(int player) {
    return new Move(player, MoveType.NO_ANTE, 0, 0);
  }

  /**
   * Get a small blind move
   *
   * @param player the move's player
   * @param value the small blind value
   * @return the small blind move
   */
  public static Move getSb(int player, int value) {
    return new Move(player, MoveType.SB, value, 0);
  }

  /**
   * Get a big blind move
   *
   * @param player the move's player
   * @param value the big blind value
   * @return the big blind move
   */
  public static Move getBb(int player, int value) {
    return new Move(player, MoveType.BB, value, 0);
  }

  /**
   * Get an blind pay refuse move
   *
   * @param player the move's player
   * @return the no-blind move
   */
  public static Move getNoBlind(int player) {
    return new Move(player, MoveType.NO_BLIND, 0, 0);
  }

  @Override
  public String toString() {
    if (type == MoveType.CALL && oldBet == value) {
      return "CHECK";
    }
    final StringBuilder builder = new StringBuilder();
    builder.append(type.toString());
    switch (type) {
      case ANTE:
      case SB:
      case BB:
      case BET:
      case CALL:
      case RAISE:
        builder.append(' ');
        builder.append(this.value);
        break;
      case FOLD:
      case NO_ANTE:
      case NO_BLIND:
        break;
    }
    return builder.toString();
  }
}

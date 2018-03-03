/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Pierre Mardon
 *
 */
@AllArgsConstructor
@ToString
public class NoBetPlayerData {

  @Getter
  private final int player;

  @Getter
  private final int stack;

  @Getter
  private final boolean inHand;

  public PlayerData getPlayerData() {
    return new PlayerData(player, stack, inHand, 0);
  }
}

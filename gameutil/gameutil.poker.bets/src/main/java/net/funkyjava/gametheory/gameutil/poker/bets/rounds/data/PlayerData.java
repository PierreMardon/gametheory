/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Pierre Mardon
 *
 */
@AllArgsConstructor
@EqualsAndHashCode
public class PlayerData {

  @Getter
  private final int player;

  @Getter
  private final int stack;

  @Getter
  private final boolean inHand;

  @Getter
  private final int bet;
}

/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Pierre Mardon
 *
 */
@AllArgsConstructor
public class BetRoundStartData {

  @Getter
  private final int firstPlayerIndex;
  @Getter
  private final int bigBlind;
}

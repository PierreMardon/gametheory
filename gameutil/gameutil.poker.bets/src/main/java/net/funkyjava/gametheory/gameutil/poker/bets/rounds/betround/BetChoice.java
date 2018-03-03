/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Represents a choice a player has to make, containing all options : bet, raise call but fold
 * (always available)
 *
 * @author Pierre Mardon
 *
 */
@AllArgsConstructor
@ToString
public class BetChoice {

  @Getter
  @NonNull
  private final BetRange betRange;

  @Getter
  @NonNull
  private final CallValue callValue;

  @Getter
  @NonNull
  private final RaiseRange raiseRange;

  @Getter
  private final int player;

}

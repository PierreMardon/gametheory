/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Builder;

/**
 * @author Pierre Mardon
 *
 */
@Builder
@AllArgsConstructor
public class BlindsAnteSpec {

  /**
   * Enable ante
   */
  @Getter
  private final boolean enableAnte;

  /**
   * Enable blinds
   */
  @Getter
  private final boolean enableBlinds;

  /**
   * Is cash game
   */
  @Getter
  private final boolean isCash;

  /**
   * Small blind value
   */
  @Getter
  private final int sbValue;

  /**
   * Big blind value
   */
  @Getter
  private final int bbValue;

  /**
   * Ante value
   */
  @Getter
  private final int anteValue;

  /**
   * List of players that should pay a BB to play this hand
   */
  @Getter
  @NonNull
  private final List<Integer> playersHavingToPayEnteringBB;

  /**
   * The small blind player, may be null
   */
  @Getter
  private final Integer sbPlayer;

  @Getter
  private final Integer bbPlayer;
}

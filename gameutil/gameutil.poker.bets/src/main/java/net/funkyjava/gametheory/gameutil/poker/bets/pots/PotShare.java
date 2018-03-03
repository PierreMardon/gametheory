/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.pots;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Share from a pot earned by a player
 *
 * @author Pierre Mardon
 *
 * @param <Id> the players ids class
 */
@AllArgsConstructor
@EqualsAndHashCode
public class PotShare {
  @Getter
  private final int value;
  @Getter
  private final int player;

}

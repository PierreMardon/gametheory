package net.funkyjava.gametheory.extensiveformgame;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An action node in the game tree
 * 
 * @author Pierre Mardon
 *
 * @param <Id>
 */
@AllArgsConstructor
@Data
public class PlayerNode<Id> {

  /**
   * The acting player index
   */
  private final int player;
  /**
   * The round index
   */
  private final int round;
  /**
   * Number of possible actions for the player
   */
  private final int nbActions;
  /**
   * An id to carry whatever needed
   */
  private final Id id;
}

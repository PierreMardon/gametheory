package net.funkyjava.gametheory.extensiveformgame;

/**
 * 
 * The extensive form game representation. Choice has been made to separate the action tree and the
 * chances states, and to represent different chances from the player point-of-vue, indexing groups
 * of chances by round to easily fit poker games. Though the rounds can be used to represent any
 * other type of chances states groups like a dice draw visible for only one player.
 * 
 * @author Pierre Mardon
 *
 * @param <Id> the action node id class
 * @param <Chances> the chances class
 */
public interface Game<Id, Chances> {

  /**
   * First index is the round, second index is the player, should contain the number of possible
   * chances for each player in each round
   *
   * @return Arrays of rounds players chances count.
   */
  int[][] roundChancesSizes();

  /**
   * The number of players
   *
   * @return number of players
   */
  int getNbPlayers();

  /**
   * Get the game root node to walk the actions and payouts tree
   *
   * @return The game state walker
   */
  ActionTreeNode<Id, Chances> rootNode();

}

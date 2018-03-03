package net.funkyjava.gametheory.extensiveformgame;

import lombok.Getter;

/**
 * An action tree node is a {@link ActionTreeNodeState} with enough additional on player nodes to
 * build a full tree
 * 
 * @author Pierre Mardon
 *
 * @param <Id> the player node id type
 * @param <Chances> the chances type
 */
public class ActionTreeNode<Id, Chances> extends ActionTreeNodeState<Id, Chances> {

  /**
   * The transition for player nodes or null
   */
  @Getter
  private final ActionTreePlayerChoiceTransition<Id, Chances> transition;
  /**
   * On a player node, meaning imperfect recall may happen on it
   */
  @Getter
  private final boolean playerNodeHasMultipleParents;


  /**
   * @see ActionTreeNodeState#ActionTreeNodeState(ChancesPayouts)
   * @param chancesPayouts
   */
  public ActionTreeNode(ChancesPayouts<Chances> chancesPayouts) {
    super(chancesPayouts);
    transition = null;
    playerNodeHasMultipleParents = false;
  }

  /**
   * @see ActionTreeNodeState#ActionTreeNodeState(double[])
   * @param payoutsNoChance
   */
  public ActionTreeNode(final double[] payoutsNoChance) {
    super(payoutsNoChance);
    transition = null;
    playerNodeHasMultipleParents = false;
  }

  /**
   * @see ActionTreeNodeState#ActionTreeNodeState(PlayerNode)
   * 
   * @param playerNode the player node
   * @param hasMultipleParents for imperfect recall purpose, provide true when it is possible that
   *        this node has multiple parents
   * @param transition the transition allowing access to children nodes
   */
  public ActionTreeNode(final PlayerNode<Id> playerNode, final boolean hasMultipleParents,
      final ActionTreePlayerChoiceTransition<Id, Chances> transition) {
    super(playerNode);
    playerNodeHasMultipleParents = hasMultipleParents;
    this.transition = transition;
  }
}

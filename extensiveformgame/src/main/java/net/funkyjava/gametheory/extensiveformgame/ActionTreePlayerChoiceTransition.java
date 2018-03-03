package net.funkyjava.gametheory.extensiveformgame;

/**
 * Access to children nodes from a player node
 * 
 * @author Pierre Mardon
 *
 * @param <Id> the player nodes id type
 * @param <Chances> the chances type
 */
public interface ActionTreePlayerChoiceTransition<Id, Chances> {

  /**
   * Access children nodes from their index. Returned node can be generated on each call for
   * convenience (there's no strict equality required)
   * 
   * @param actionIndex the index of the action
   * @return the node for the provided action index
   */
  ActionTreeNode<Id, Chances> nodeForAction(final int actionIndex);
}

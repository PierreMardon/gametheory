package net.funkyjava.gametheory.extensiveformgame;

import lombok.Getter;

/**
 * Polymorph representation of an action state
 * 
 * @author Pierre Mardon
 *
 * @param <Id> the player nodes id type
 * @param <Chances> the chances type
 */
public class ActionTreeNodeState<Id, Chances> {
  /**
   * The action tree node type enum
   * 
   * @author Pierre Mardon
   *
   */
  public static enum NodeType {
    /**
     * A player type node is a node at which a player should make a decision
     */
    PLAYER,
    /**
     * Terminal node type with constant utility
     */
    PAYOUTS_NO_CHANCE,
    /**
     * Terminal node type with utility depending on chances
     */
    CHANCES_PAYOUTS
  }

  /**
   * Node type
   */
  @Getter
  private final NodeType nodeType;
  /**
   * Player node when the node type matches or null
   */
  @Getter
  private final PlayerNode<Id> playerNode;
  /**
   * The chances independent payouts when the node type matches or null
   */
  @Getter
  private final double[] payoutsNoChance;
  /**
   * The chances dependent payouts when the node type matches or null
   */
  @Getter
  private final ChancesPayouts<Chances> chancesPayouts;

  /**
   * Constructor for a player node
   * 
   * @param playerNode the player node
   */
  public ActionTreeNodeState(final PlayerNode<Id> playerNode) {
    this.nodeType = NodeType.PLAYER;
    this.playerNode = playerNode;
    this.payoutsNoChance = null;
    this.chancesPayouts = null;
  }

  /**
   * Constructor for a terminal node chances independent
   * 
   * @param payoutsNoChance
   */
  public ActionTreeNodeState(final double[] payoutsNoChance) {
    this.nodeType = NodeType.PAYOUTS_NO_CHANCE;
    this.playerNode = null;
    this.payoutsNoChance = payoutsNoChance;
    this.chancesPayouts = null;
  }

  /**
   * Constructor for a chances dependent node
   * 
   * @param chancesPayouts
   */
  public ActionTreeNodeState(final ChancesPayouts<Chances> chancesPayouts) {
    this.nodeType = NodeType.CHANCES_PAYOUTS;
    this.playerNode = null;
    this.payoutsNoChance = null;
    this.chancesPayouts = chancesPayouts;
  }
}

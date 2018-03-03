package net.funkyjava.gametheory.cscfrm;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.AtomicDoubleArray;

import lombok.Getter;
import net.funkyjava.gametheory.extensiveformgame.ActionChancesData;
import net.funkyjava.gametheory.extensiveformgame.ActionTree;
import net.funkyjava.gametheory.extensiveformgame.ActionTreeNodeState.NodeType;
import net.funkyjava.gametheory.extensiveformgame.Game;
import net.funkyjava.gametheory.extensiveformgame.LinkedActionTreeNode;
import net.funkyjava.gametheory.extensiveformgame.PlayerNode;
import net.funkyjava.gametheory.io.Fillable;
import net.funkyjava.gametheory.io.IOUtils;

/**
 * 
 * @author Pierre Mardon
 *
 * @param <Id> the player nodes id class
 * @param <Chances> the chances class
 */
public class CSCFRMData<Id, Chances> implements Fillable {
  /**
   * Number of CSCFRM iterations performed so long
   */
  @Getter
  private final AtomicLong iterations = new AtomicLong();
  /**
   * Utility sum of the strategies
   */
  @Getter
  private final AtomicDoubleArray utilitySum;
  /**
   * The CSCFRM nodes of the game indexed by round, player, chance, actions state
   */
  @Getter
  private final CSCFRMNode[][][][] nodes;
  /**
   * Chances sizes indexed by round, player
   */
  @Getter
  private final int[][] roundChancesSizes;
  /**
   * Number of players
   */
  @Getter
  private final int nbPlayers;
  /**
   * Action tree of the game
   */
  @Getter
  private final ActionTree<Id, Chances> gameActionTree;

  @Getter
  private final Map<LinkedActionTreeNode<Id, ?>, CSCFRMNode[]> nodesForEachActionNode;

  /**
   * Constructor. Builds the action tree from the game and generates the CSCFRM nodes.
   * 
   * @param game
   */
  public CSCFRMData(final Game<Id, Chances> game) {
    this.nbPlayers = game.getNbPlayers();
    this.roundChancesSizes = game.roundChancesSizes();
    final ActionTree<Id, Chances> actionTree = this.gameActionTree = new ActionTree<>(game);
    this.nodes = ActionChancesData.createRoundPlayerChanceNodeData(actionTree, game,
        new CSCFRMNodeProvider<Id>());
    final int nbPlayers = game.getNbPlayers();
    this.utilitySum = new AtomicDoubleArray(nbPlayers);
    this.nodesForEachActionNode = nodesForEachActionNode();
  }

  @Override
  public void fill(InputStream is) throws IOException {
    final DataInputStream dis = new DataInputStream(is);
    iterations.set(dis.readLong());
    final int nbPlayers = this.nbPlayers;
    final AtomicDoubleArray utilitySum = this.utilitySum;
    for (int i = 0; i < nbPlayers; i++) {
      utilitySum.set(i, dis.readDouble());
    }
    IOUtils.fill(is, nodes);
  }

  @Override
  public void write(OutputStream os) throws IOException {
    final DataOutputStream dos = new DataOutputStream(os);
    dos.writeLong(iterations.longValue());
    final AtomicDoubleArray utilitySum = this.utilitySum;
    for (int i = 0; i < nbPlayers; i++) {
      dos.writeDouble(utilitySum.get(i));
    }
    IOUtils.write(os, nodes);
  }

  /**
   * Builds the map between each action node and the array of CSCFRM nodes for all chances
   * 
   * @return the CSCFRM nodes associated with each action node
   */
  public Map<LinkedActionTreeNode<Id, ?>, CSCFRMNode[]> nodesForEachActionNode() {
    final LinkedHashMap<LinkedActionTreeNode<Id, ?>, CSCFRMNode[]> res = new LinkedHashMap<>();
    for (LinkedActionTreeNode<Id, ?>[][] roundNodes : gameActionTree.getActionNodes()) {
      for (LinkedActionTreeNode<Id, ?>[] playerNodes : roundNodes) {
        for (LinkedActionTreeNode<Id, ?> node : playerNodes) {
          res.put(node, nodesFor(node));
        }
      }
    }
    return res;
  }

  /**
   * Get all CSCFRM nodes for a specified action node
   * 
   * @param node the action node
   * @return array of CSCFRM nodes for all chances
   */
  public CSCFRMNode[] nodesFor(final LinkedActionTreeNode<Id, ?> node) {
    checkArgument(node.getNodeType() == NodeType.PLAYER, "CSCFRM data only for player nodes");
    final PlayerNode<Id> pNode = node.getPlayerNode();
    final int round = pNode.getRound();
    final int player = pNode.getPlayer();
    final int index = node.getPlayerRoundActionIndex();
    final int nbChances = roundChancesSizes[round][player];
    final CSCFRMNode[] res = new CSCFRMNode[nbChances];
    final CSCFRMNode[][] playerNodes = nodes[round][player];
    for (int i = 0; i < nbChances; i++) {
      res[i] = playerNodes[i][index];
    }
    return res;
  }

  /**
   * Compute the utility average of the game in the current state
   * 
   * @return the utility average
   */
  public double[] getUtilityAvg() {
    final int nbPlayers = this.nbPlayers;
    final double[] res = new double[nbPlayers];
    final long iterations = this.iterations.get();
    final AtomicDoubleArray utilitySum = this.utilitySum;
    for (int i = 0; i < nbPlayers; i++) {
      res[i] = utilitySum.get(i) / iterations;
    }
    return res;
  }
}

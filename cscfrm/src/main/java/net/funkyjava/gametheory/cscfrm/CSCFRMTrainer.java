package net.funkyjava.gametheory.cscfrm;

import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.AtomicDoubleArray;

import net.funkyjava.gametheory.extensiveformgame.ActionTree;
import net.funkyjava.gametheory.extensiveformgame.LinkedActionTreeNode;
import net.funkyjava.gametheory.extensiveformgame.PlayerNode;

/**
 * A trainer performs CSCFRM iterations, assuming that there will be no chances collision. It
 * internally holds depth indexed reusable arrays to avoid creating them at each iteration
 * 
 * @author Pierre Mardon
 *
 * @param <Chances> the chances class
 */
public class CSCFRMTrainer<Chances extends CSCFRMChances> {

  private final int nbRounds;
  private final int nbPlayers;
  private final LinkedActionTreeNode<?, Chances> rootNode;
  private final CSCFRMNode[][][][] nodes;
  private final CSCFRMNode[][][] chancesNodes;
  private final AtomicDoubleArray utilitySum;
  private final AtomicLong iterations;
  private final double[] realizationWeights;
  private final double[][] depthUtil;
  private final double[][] depthActionUtil;
  private final double[][] depthStrategy;
  private final double[] zero;
  private final double[] one;

  /**
   * Constructor
   * 
   * @param data the CSCFRM data
   */
  public CSCFRMTrainer(final CSCFRMData<?, Chances> data) {
    final ActionTree<?, Chances> actionTree = data.getGameActionTree();
    final int maxDepth = actionTree.getMaxDepth();
    final int maxNbActions = actionTree.getMaxNbActions();
    final int nbRounds = this.nbRounds = data.getRoundChancesSizes().length;
    final int nbPlayers = this.nbPlayers = data.getNbPlayers();
    this.utilitySum = data.getUtilitySum();
    this.iterations = data.getIterations();
    rootNode = actionTree.getRoot();
    nodes = data.getNodes();
    chancesNodes = new CSCFRMNode[nbRounds][nbPlayers][];
    zero = new double[Math.max(nbPlayers, maxNbActions)];
    one = new double[nbPlayers];
    for (int i = 0; i < nbPlayers; i++) {
      one[i] = 1;
    }
    depthUtil = new double[maxDepth][nbPlayers];
    depthActionUtil = new double[maxDepth][maxNbActions];
    depthStrategy = new double[maxDepth][maxNbActions];
    realizationWeights = new double[nbPlayers];
  }

  /**
   * Iterates CSCFRM for given chances
   * 
   * @param chances the chances
   */
  public final void train(final Chances chances) {
    // Get the nodes we need for this iteration given the provided chances
    final int nbRounds = this.nbRounds;
    final int nbPlayers = this.nbPlayers;
    final CSCFRMNode[][][][] nodes = this.nodes;
    final CSCFRMNode[][][] chancesNodes = this.chancesNodes;
    final int[][] playersChances = chances.getPlayersChances();
    for (int round = 0; round < nbRounds; round++) {
      final int[] roundChances = playersChances[round];
      final CSCFRMNode[][][] roundNodes = nodes[round];
      final CSCFRMNode[][] roundChancesNodes = chancesNodes[round];
      for (int player = 0; player < nbPlayers; player++) {
        roundChancesNodes[player] = roundNodes[player][roundChances[player]];
      }
    }
    final double[] realizationWeights = this.realizationWeights;
    System.arraycopy(one, 0, realizationWeights, 0, nbPlayers);
    final double[] utility = rec(0, rootNode, chances, realizationWeights);
    final AtomicDoubleArray utilitySum = this.utilitySum;
    for (int i = 0; i < nbPlayers; i++) {
      utilitySum.addAndGet(i, utility[i]);
    }
    iterations.incrementAndGet();
  }

  private final double[] rec(final int depth, final LinkedActionTreeNode<?, Chances> node,
      final Chances chances, final double[] realizationWeights) {
    switch (node.getNodeType()) {

      case PAYOUTS_NO_CHANCE:
        return node.getPayoutsNoChance();

      case CHANCES_PAYOUTS:
        return node.getChancesPayouts().getPayouts(chances);

      case PLAYER:
        final int nbPlayers = this.nbPlayers;
        final PlayerNode<?> pNode = node.getPlayerNode();
        final int index = node.getPlayerRoundActionIndex();
        final int round = pNode.getRound();
        final int player = pNode.getPlayer();
        final CSCFRMNode csNode = chancesNodes[round][player][index];

        final int nbChildren = pNode.getNbActions();
        final LinkedActionTreeNode<?, Chances>[] children = node.getChildren();
        final double[] stratSum = csNode.getStrategySum();
        final double[] regretSum = csNode.getRegretSum();
        final double[] zero = this.zero;
        final double[] strategy = depthStrategy[depth];
        System.arraycopy(zero, 0, strategy, 0, nbChildren);
        final double[] util = depthUtil[depth];
        System.arraycopy(zero, 0, util, 0, nbPlayers);
        final double[] actionsUtil = depthActionUtil[depth];
        System.arraycopy(zero, 0, actionsUtil, 0, nbChildren);

        double totalRegret = 0;
        for (int action = 0; action < nbChildren; action++) {
          double actionRegret = regretSum[action];
          if (actionRegret < 0) {
            actionRegret = regretSum[action] = 0;
          }
          // CFR+ regret matching
          totalRegret += strategy[action] = (actionRegret > 0 ? actionRegret : 0);
        }
        final double playerRealWeight = realizationWeights[player];
        if (totalRegret > 0) {
          for (int action = 0; action < nbChildren; action++) {
            stratSum[action] += playerRealWeight * (strategy[action] /= totalRegret);
          }
        } else {
          for (int action = 0; action < nbChildren; action++) {
            stratSum[action] += playerRealWeight * (strategy[action] = (1.0d / nbChildren));
          }
        }
        double weight = 1;
        for (int p = 0; p < nbPlayers; p++) {
          if (p != player) {
            weight *= realizationWeights[p];
          }
        }
        final int nextDepth = depth + 1;
        for (int action = 0; action < nbChildren; action++) {
          final double oldReal = realizationWeights[player];
          realizationWeights[player] *= strategy[action];
          final double[] childUtil = rec(nextDepth, children[action], chances, realizationWeights);
          for (int p = 0; p < nbPlayers; p++) {
            util[p] += strategy[action] * childUtil[p];
          }
          actionsUtil[action] = childUtil[player];
          realizationWeights[player] = oldReal;
        }
        final double playerUtil = util[player];
        for (int action = 0; action < nbChildren; action++) {
          regretSum[action] += weight * (actionsUtil[action] - playerUtil);
        }
        return util;
    }
    return null;
  }

}

package net.funkyjava.gametheory.games.nlhe.preflop;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.functions.T;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.cscfrm.CSCFRMData;
import net.funkyjava.gametheory.cscfrm.CSCFRMNode;
import net.funkyjava.gametheory.extensiveformgame.ActionTreeNodeState.NodeType;
import net.funkyjava.gametheory.extensiveformgame.LinkedActionTreeNode;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.CardsGroupsIndexer;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.MoveType;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLBetTreeNode;

@Slf4j
public class HEPreflopHelper {

  private HEPreflopHelper() {}

  public static final String[][] canonicalPreflopHandNames = {
      {"AA", "AKs", "AQs", "AJs", "ATs", "A9s", "A8s", "A7s", "A6s", "A5s", "A4s", "A3s", "A2s"},
      {"AKo", "KK", "KQs", "KJs", "KTs", "K9s", "K8s", "K7s", "K6s", "K5s", "K4s", "K3s", "K2s"},
      {"AQo", "KQo", "QQ", "QJs", "QTs", "Q9s", "Q8s", "Q7s", "Q6s", "Q5s", "Q4s", "Q3s", "Q2s"},
      {"AJo", "KJo", "QJo", "JJ", "JTs", "J9s", "J8s", "J7s", "J6s", "J5s", "J4s", "J3s", "J2s"},
      {"ATo", "KTo", "QTo", "JTo", "TT", "T9s", "T8s", "T7s", "T6s", "T5s", "T4s", "T3s", "T2s"},
      {"A9o", "K9o", "Q9o", "J9o", "T9o", "99", "98s", "97s", "96s", "95s", "94s", "93s", "92s"},
      {"A8o", "K8o", "Q8o", "J8o", "T8o", "98o", "88", "87s", "86s", "85s", "84s", "83s", "82s"},
      {"A7o", "K7o", "Q7o", "J7o", "T7o", "97o", "87o", "77", "76s", "75s", "74s", "73s", "72s"},
      {"A6o", "K6o", "Q6o", "J6o", "T6o", "96o", "86o", "76o", "66", "65s", "64s", "63s", "62s"},
      {"A5o", "K5o", "Q5o", "J5o", "T5o", "95o", "85o", "75o", "65o", "55", "54s", "53s", "52s"},
      {"A4o", "K4o", "Q4o", "J4o", "T4o", "94o", "84o", "74o", "64o", "54o", "44", "43s", "42s"},
      {"A3o", "K3o", "Q3o", "J3o", "T3o", "93o", "83o", "73o", "63o", "53o", "43o", "33", "32s"},
      {"A2o", "K2o", "Q2o", "J2o", "T2o", "92o", "82o", "72o", "62o", "52o", "42o", "32o", "22"},};

  @SuppressWarnings("unchecked")
  public static <U> U[][] canonicalArrayChancesIndexed(final U[] objects,
      final int[][] chancesCanonicalCoordinates) {
    final int length = objects.length;
    final U[][] result = (U[][]) Array.newInstance(objects.getClass(), 13);
    for (int i = 0; i < 13; i++) {
      result[i] = (U[]) Array.newInstance(objects[0].getClass(), 13);
    }
    for (int i = 0; i < length; i++) {
      final int[] coord = chancesCanonicalCoordinates[i];
      result[coord[0]][coord[1]] = objects[i];
    }
    return result;
  }

  public static int[][] chancesCanonicalCoordinates(final CardsGroupsIndexer preflopIndexer) {
    final int[][] cards = new int[1][2];
    final int[][] res = new int[preflopIndexer.getIndexSize()][];
    final IntCardsSpec specs = preflopIndexer.getCardsSpec();
    for (int r1 = 0; r1 < 13; r1++) {
      // Pair
      cards[0][0] = specs.getCard(r1, 0);
      cards[0][1] = specs.getCard(r1, 1);
      final int pairIndex = preflopIndexer.indexOf(cards);
      res[pairIndex] = new int[] {12 - r1, 12 - r1};
      for (int r2 = r1 + 1; r2 < 13; r2++) {
        // Suited above, r2 is higher so the row index is less than the column index
        cards[0][1] = specs.getCard(r2, 0);
        final int suitedIndex = preflopIndexer.indexOf(cards);
        res[suitedIndex] = new int[] {12 - r2, 12 - r1};
        // Offsuit below
        cards[0][1] = specs.getCard(r2, 1);
        final int offSuitIndex = preflopIndexer.indexOf(cards);
        res[offSuitIndex] = new int[] {12 - r1, 12 - r2};
      }
    }
    return res;
  }

  public static double[][] getMoveStrategy(final int moveIndex, final CSCFRMNode[] chanceNodes,
      CardsGroupsIndexer preflopIndexer) {
    final int[][] indexes = chancesCanonicalCoordinates(preflopIndexer);
    final CSCFRMNode[][] nodes = canonicalArrayChancesIndexed(chanceNodes, indexes);
    final int nbLines = nodes.length;
    final double[][] strat = new double[nbLines][];
    for (int i = 0; i < nbLines; i++) {
      final CSCFRMNode[] line = nodes[i];
      final int lineSize = line.length;
      final double[] lineStrat = strat[i] = new double[lineSize];
      for (int j = 0; j < lineSize; j++) {
        final CSCFRMNode node = line[j];
        lineStrat[j] = node.getAvgStrategy()[moveIndex];
      }
    }
    return strat;
  }

  public static List<double[][]> playerParentsStrategies(
      final LinkedActionTreeNode<NLBetTreeNode, ?> node, final CSCFRMData<NLBetTreeNode, ?> data,
      CardsGroupsIndexer preflopIndexer) {
    final int player = node.getPlayerNode().getPlayer();
    final Map<LinkedActionTreeNode<NLBetTreeNode, ?>, CSCFRMNode[]> allNodes =
        data.nodesForEachActionNode();
    final List<double[][]> res = new ArrayList<>();
    LinkedActionTreeNode<NLBetTreeNode, ?> currentNode = node;
    while (!currentNode.getParents().isEmpty()) {
      final LinkedActionTreeNode<NLBetTreeNode, ?> parent = currentNode.getParents().get(0);
      if (parent.getNodeType() != NodeType.PLAYER || parent.getPlayerNode().getPlayer() != player) {
        currentNode = parent;
        continue;
      }
      final LinkedActionTreeNode<NLBetTreeNode, ?>[] children = parent.getChildren();
      final int nbChildren = children.length;
      for (int i = 0; i < nbChildren; i++) {
        if (children[i] == currentNode) {
          res.add(getMoveStrategy(i, allNodes.get(parent), preflopIndexer));
          currentNode = parent;
        }
      }
    }
    return Lists.reverse(res);
  }

  public static Map<Move, double[][]> getMovesStrategies(
      final LinkedActionTreeNode<NLBetTreeNode, ?> node, final CSCFRMNode[] chanceNodes,
      final CardsGroupsIndexer preflopIndexer) {
    final Map<Move, double[][]> strats = new LinkedHashMap<>();
    LinkedHashMap<Move, NLBetTreeNode> children = node.getPlayerNode().getId().getChildren();
    int childIndex = 0;
    for (Move move : children.keySet()) {
      strats.put(move, getMoveStrategy(childIndex, chanceNodes, preflopIndexer));
      childIndex++;
    }
    return strats;
  }

  public static Map<NLBetTreeNode, Map<Move, double[][]>> getStrategies(
      final CSCFRMData<NLBetTreeNode, ?> data, final CardsGroupsIndexer holeCardsIndexer,
      final Map<T, String> playersNames) {
    final LinkedHashMap<NLBetTreeNode, Map<Move, double[][]>> res = new LinkedHashMap<>();
    Map<LinkedActionTreeNode<NLBetTreeNode, ?>, CSCFRMNode[]> allNodes =
        data.nodesForEachActionNode();
    for (LinkedActionTreeNode<NLBetTreeNode, ?> actionNode : allNodes.keySet()) {
      final CSCFRMNode[] nodes = allNodes.get(actionNode);
      res.put(actionNode.getPlayerNode().getId(),
          getMovesStrategies(actionNode, nodes, holeCardsIndexer));
    }
    return res;
  }

  public static void printMovePureStrategy(final int moveIndex, final CSCFRMNode[] chanceNodes,
      CardsGroupsIndexer preflopIndexer) {
    final int[][] indexes = chancesCanonicalCoordinates(preflopIndexer);
    final String[][] strs = canonicalPreflopHandNames;
    final StringBuilder builder = new StringBuilder();
    final CSCFRMNode[][] nodes = canonicalArrayChancesIndexed(chanceNodes, indexes);
    for (int i = 0; i < nodes.length; i++) {
      final CSCFRMNode[] line = nodes[i];
      for (int j = 0; j < line.length; j++) {
        final CSCFRMNode node = line[j];
        if (node.getAvgStrategy()[moveIndex] >= 0.5) {
          builder.append(strs[i][j]);
        }
        builder.append('\t');
      }
      builder.append('\n');
    }
    log.info("\n" + builder.toString());
  }

  public static void printStrategy(final LinkedActionTreeNode<NLBetTreeNode, ?> node,
      final CSCFRMNode[] chanceNodes, final CardsGroupsIndexer preflopIndexer) {
    printStrategy(node, chanceNodes, preflopIndexer, Collections.<Integer, String>emptyMap());
  }

  public static void printStrategy(final LinkedActionTreeNode<NLBetTreeNode, ?> node,
      final CSCFRMNode[] chanceNodes, final CardsGroupsIndexer preflopIndexer,
      final Map<Integer, String> playersNames) {
    if (node.getPlayerNode().getRound() != 0) {
      // Preflop only
      return;
    }
    final NLBetTreeNode betNode = node.getPlayerNode().getId();
    final NLHand hand = betNode.getHand();
    String name = playersNames.get(hand.getBettingPlayer());
    if (name == null) {
      name = hand.getBettingPlayer().toString();
    }
    log.info("##################################################################");
    log.info(hand.movesString(playersNames) + " | Active player " + name);
    log.info("##################################################################");
    LinkedHashMap<Move, NLBetTreeNode> children = betNode.getChildren();
    int childIndex = 0;
    for (Move move : children.keySet()) {
      if (move.getType() == MoveType.FOLD) {
        childIndex++;
        continue;
      }
      log.info("### " + move + " strategy :");
      HEPreflopHelper.printMovePureStrategy(childIndex, chanceNodes, preflopIndexer);
      log.info("");
      childIndex++;
    }
  }

  public static void printStrategies(final CSCFRMData<NLBetTreeNode, ?> data,
      final CardsGroupsIndexer holeCardsIndexer) {
    printStrategies(data, holeCardsIndexer, Collections.<Integer, String>emptyMap());
  }

  public static void printStrategies(final CSCFRMData<NLBetTreeNode, ?> data,
      final CardsGroupsIndexer holeCardsIndexer, final Map<Integer, String> playersNames) {
    Map<LinkedActionTreeNode<NLBetTreeNode, ?>, CSCFRMNode[]> allNodes =
        data.nodesForEachActionNode();
    for (LinkedActionTreeNode<NLBetTreeNode, ?> actionNode : allNodes.keySet()) {
      final CSCFRMNode[] nodes = allNodes.get(actionNode);
      HEPreflopHelper.printStrategy(actionNode, nodes, holeCardsIndexer, playersNames);
      log.info("");
    }
    final long iterations = data.getIterations().longValue();
    log.info("Nb iterations : {}", iterations);
    log.info("Utility : {}", data.getUtilityAvg());
  }
}

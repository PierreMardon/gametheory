package net.funkyjava.gametheory.extensiveformgame;

import java.lang.reflect.Array;

/**
 * Convenience class to easily create chances data for a given action tree
 * 
 * @author Pierre Mardon
 *
 * @param <NodeData> the data to create class
 */
public class ActionChancesData<NodeData> {

  /**
   * A data provider provides the class of the data that will be produced and a method to create
   * data for a given action node / chance combination
   * 
   * @author Pierre Mardon
   *
   * @param <NodeData> the type of the provided data
   * @param <Id> the player nodes id type
   */
  public static interface DataProvider<NodeData, Id> {
    /**
     * The class of the produced data
     * 
     * @return The class of the produced data
     */
    Class<NodeData> getDataClass();

    /**
     * Provides the data for the given game's action node and chance
     * 
     * @param game the game
     * @param node the action node
     * @param chance the chance
     * @return the associated data
     */
    NodeData getData(final Game<Id, ?> game, final LinkedActionTreeNode<Id, ?> node,
        final int chance);
  }

  private ActionChancesData() {

  }

  /**
   *
   * Creates data in an array indexed successively by the round index, the player index, the chance
   * index, and the player's round's node index
   *
   * @param actionTree the action tree
   * @param game the game
   * @param provider the data provider
   * @return the four dimensions array
   */
  @SuppressWarnings("unchecked")
  public static <NodeData, Id> NodeData[][][][] createRoundPlayerChanceNodeData(
      final ActionTree<Id, ?> actionTree, final Game<Id, ?> game,
      final DataProvider<NodeData, Id> provider) {
    final Class<NodeData> dataClass = provider.getDataClass();
    final Class<?> oneDimensionArrayClass = Array.newInstance(dataClass, 0).getClass();
    final Class<?> twoDimensionsArrayClass =
        Array.newInstance(oneDimensionArrayClass, 0).getClass();
    final Class<?> threeDimensionsArrayClass =
        Array.newInstance(twoDimensionsArrayClass, 0).getClass();
    final int[][] chancesSize = game.roundChancesSizes();
    final int nbRounds = chancesSize.length;
    final int nbPlayers = game.getNbPlayers();
    final NodeData[][][][] data =
        (NodeData[][][][]) Array.newInstance(threeDimensionsArrayClass, nbRounds);
    for (int round = 0; round < nbRounds; round++) {
      final int[] roundChancesSize = chancesSize[round];
      final NodeData[][][] roundData =
          data[round] = (NodeData[][][]) Array.newInstance(twoDimensionsArrayClass, nbPlayers);
      for (int player = 0; player < nbPlayers; player++) {
        final int nbChances = roundChancesSize[player];
        final LinkedActionTreeNode<Id, ?>[] actionNodes =
            actionTree.getActionNodes()[round][player];
        final int nbNodes = actionNodes.length;
        final NodeData[][] playerData =
            roundData[player] = (NodeData[][]) Array.newInstance(oneDimensionArrayClass, nbChances);
        for (int chance = 0; chance < nbChances; chance++) {
          final NodeData[] chanceData =
              playerData[chance] = (NodeData[]) Array.newInstance(dataClass, nbNodes);
          for (int nodeIndex = 0; nodeIndex < nbNodes; nodeIndex++) {
            chanceData[nodeIndex] = provider.getData(game, actionNodes[nodeIndex], chance);
          }
        }
      }
    }
    return data;
  }

  /**
   *
   * Creates data in an array indexed successively by the round index, the player index, the
   * player's round's node index and the chance index
   *
   * @param actionTree the action tree
   * @param game the game
   * @param provider the data provider
   * @return the four dimensions array
   */
  @SuppressWarnings("unchecked")
  public static <NodeData, Id> NodeData[][][][] createRoundPlayerNodeChanceData(
      final ActionTree<Id, ?> actionTree, final Game<Id, ?> game,
      final DataProvider<NodeData, Id> provider) {
    final Class<NodeData> dataClass = provider.getDataClass();
    final Class<?> oneDimensionArrayClass = Array.newInstance(dataClass, 0).getClass();
    final Class<?> twoDimensionsArrayClass =
        Array.newInstance(oneDimensionArrayClass, 0).getClass();
    final Class<?> threeDimensionsArrayClass =
        Array.newInstance(twoDimensionsArrayClass, 0).getClass();
    final int[][] chancesSize = game.roundChancesSizes();
    final int nbRounds = chancesSize.length;
    final int nbPlayers = game.getNbPlayers();
    final NodeData[][][][] data =
        (NodeData[][][][]) Array.newInstance(threeDimensionsArrayClass, nbRounds);
    for (int round = 0; round < nbRounds; round++) {
      final int[] roundChancesSize = chancesSize[round];
      final NodeData[][][] roundData =
          data[round] = (NodeData[][][]) Array.newInstance(twoDimensionsArrayClass, nbPlayers);
      for (int player = 0; player < nbPlayers; player++) {
        final int nbChances = roundChancesSize[player];
        final LinkedActionTreeNode<Id, ?>[] actionNodes =
            actionTree.getActionNodes()[round][player];
        final int nbNodes = actionNodes.length;
        final NodeData[][] playerData =
            roundData[player] = (NodeData[][]) Array.newInstance(oneDimensionArrayClass, nbNodes);
        for (int nodeIndex = 0; nodeIndex < nbNodes; nodeIndex++) {
          final NodeData[] nodeData =
              playerData[nodeIndex] = (NodeData[]) Array.newInstance(dataClass, nbChances);
          final LinkedActionTreeNode<Id, ?> node = actionNodes[nodeIndex];
          for (int chance = 0; chance < nbChances; chance++) {
            nodeData[nodeIndex] = provider.getData(game, node, chance);
          }
        }
      }
    }
    return data;
  }

}

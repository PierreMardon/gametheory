package net.funkyjava.gametheory.cscfrm;

import net.funkyjava.gametheory.extensiveformgame.ActionChancesData.DataProvider;
import net.funkyjava.gametheory.extensiveformgame.Game;
import net.funkyjava.gametheory.extensiveformgame.LinkedActionTreeNode;

/**
 * Creates CSCFRM nodes for each action node of a game
 * 
 * @author Pierre Mardon
 *
 * @param <Id> players id class
 */
public class CSCFRMNodeProvider<Id> implements DataProvider<CSCFRMNode, Id> {

  @Override
  public CSCFRMNode getData(final Game<Id, ?> game, final LinkedActionTreeNode<Id, ?> node,
      final int chance) {
    return new CSCFRMNode(node.getPlayerNode().getNbActions());
  }

  @Override
  public Class<CSCFRMNode> getDataClass() {
    return CSCFRMNode.class;
  }

}

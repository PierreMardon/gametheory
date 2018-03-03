package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import java.util.List;

import com.google.common.base.Optional;

import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;

public interface NLBetTreeWalker {

  boolean handleCurrentNode(final NLBetTreeNode node, final List<NLBetTreeNode> parents,
      final Optional<Move> lastMove);

}

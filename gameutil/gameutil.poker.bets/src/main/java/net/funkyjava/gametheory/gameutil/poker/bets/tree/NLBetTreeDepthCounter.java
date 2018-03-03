package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;

public class NLBetTreeDepthCounter implements NLBetTreeWalker {

  @Getter
  private int depth = 0;

  public NLBetTreeDepthCounter() {}

  @Override
  public boolean handleCurrentNode(NLBetTreeNode node, List<NLBetTreeNode> parents,
      final Optional<Move> lastMove) {
    depth = Math.max(depth, 1 + parents.size());
    return true;
  }

}

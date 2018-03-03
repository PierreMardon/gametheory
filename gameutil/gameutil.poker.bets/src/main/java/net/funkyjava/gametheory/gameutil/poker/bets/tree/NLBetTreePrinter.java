package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import java.util.List;

import com.google.common.base.Optional;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.Pot;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundType;

@Slf4j
public class NLBetTreePrinter implements NLBetTreeWalker {

  public NLBetTreePrinter() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public boolean handleCurrentNode(NLBetTreeNode node, List<NLBetTreeNode> parents,
      final Optional<Move> lastMoveOpt) {
    if (!lastMoveOpt.isPresent()) {
      return true;
    }
    final int depth = parents.size();
    final NLHand hand = node.getHand();
    final Move lastMove = lastMoveOpt.get();
    String moveStr = "";
    switch (lastMove.getType()) {
      case CALL:
        if (lastMove.getValue() == lastMove.getOldBet()) {
          moveStr = "CHECK " + lastMove.getValue();
        }
      case BET:
      case RAISE:
        moveStr = lastMove.getType() + " " + (lastMove.getValue());
        break;
      case FOLD:
        moveStr = "FOLD";
        break;
      case ANTE:
      case BB:
      case NO_ANTE:
      case NO_BLIND:
      case SB:
        return true;
      default:
        break;

    }

    moveStr = lastMove.getPlayer() + "- " + moveStr;
    if (parents.size() > 0) {
      final NLBetTreeNode parent = parents.get(parents.size() - 1);
      if (parent.getHand().getRoundType() == RoundType.BETS
          && parent.getHand().getRoundIndex() != node.getHand().getRoundIndex()) {
        moveStr += " NEW STREET";
      }
    }
    String str = "|";
    for (int i = 0; i < depth - 1; i++) {
      str += "\t|";
    }
    str += "__>";
    str += moveStr + " : ";
    switch (hand.getRoundState()) {
      case CANCELED:
        str += "CANCELED";
        break;
      case END_NO_SHOWDOWN:
        final int winningPlayer = hand.getNoShowdownWinningPlayer();
        str += winningPlayer + " wins " + hand.getTotalPotsValue();
        break;
      case NEXT_ROUND:
        break;
      case SHOWDOWN:
        final List<Pot> pots = hand.getCurrentPots();
        String potsStr = "Pots : ";
        for (Pot pot : pots) {
          potsStr += pot + " ";
        }
        str += "Showdown " + potsStr;
        break;
      case WAITING_MOVE:
        break;
      default:
        break;

    }
    log.info(str);
    return true;
  }

}

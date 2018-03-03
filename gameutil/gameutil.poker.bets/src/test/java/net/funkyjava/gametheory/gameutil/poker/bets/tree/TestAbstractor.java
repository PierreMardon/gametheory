package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.anteround.AnteValue;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetChoice;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetRange;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.CallValue;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.RaiseRange;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround.BlindValue;

public class TestAbstractor implements NLBetTreeAbstractor {

  public TestAbstractor() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public List<Move> movesForHand(NLHand hand) {
    final List<Move> res = new ArrayList<>();
    switch (hand.getRoundType()) {
      case ANTE:
        // Shouldn't happen, already ignored by the tree building algorithm
        final Map<Integer, AnteValue> antes = hand.getMissingAnte();
        final Integer antePlayer = antes.keySet().iterator().next();
        res.add(Move.getAnte(antePlayer, antes.get(antePlayer).getValue()));
        break;
      case BETS:
        final BetChoice choice = hand.getBetChoice();
        final Integer betsPlayer = choice.getPlayer();
        final BetRange betRange = choice.getBetRange();

        final CallValue callValue = choice.getCallValue();
        if (callValue.exists()) {
          if (!callValue.isCheck()) {
            res.add(Move.getFold(betsPlayer));
          }
          res.add(Move.getCall(betsPlayer, callValue.getValue(), callValue.getOldBet()));
        }

        if (betRange.exists()) {
          final int max = betRange.getMax();
          final int min = betRange.getMin();
          if (max != min) {
            res.add(Move.getBet(betsPlayer, Math.max((max + min) / 2, min)));
          }
          res.add(Move.getBet(betsPlayer, max));
        }

        final RaiseRange raiseRange = choice.getRaiseRange();
        if (raiseRange.exists()) {
          final int max = raiseRange.getMax();
          final int min = raiseRange.getMin();
          final int oldBet = raiseRange.getOldBet();
          if (max > min) {
            res.add(Move.getRaise(betsPlayer, Math.max((max + min) / 2, min), oldBet));
          }
          res.add(Move.getRaise(betsPlayer, max, oldBet));
        }
        break;
      case BLINDS:
        // Shouldn't happen, already ignored by the tree building algorithm
        final Map<Integer, BlindValue> blinds = hand.getMissingBlinds();
        final Integer blindsPlayer = blinds.keySet().iterator().next();
        final BlindValue blind = blinds.get(blindsPlayer);
        switch (blind.getType()) {
          case BB:
            res.add(Move.getBb(blindsPlayer, blind.getValue()));
            break;
          case SB:
            res.add(Move.getSb(blindsPlayer, blind.getValue()));
            break;
        }
        break;

    }
    return res;
  }

}

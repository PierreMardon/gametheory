package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.Pot;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.SharedPot;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundState;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundType;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayerData;

public class NLBetTreeNode {

  public static final int NO_PLAYER_INDEX = -1;

  @Getter
  private final NLHand hand;
  @Getter
  private final LinkedHashMap<Move, NLBetTreeNode> children;
  @Getter
  private final List<Move> orderedMoves;

  public final boolean isRoundFirstNode;
  public final int index;
  public final int playerIndex;
  public final int nbChildren;
  public final int betRoundIndex;
  public final NLBetTreeNode orderedChildren[];
  public final RoundState roundState;

  @SuppressWarnings("unchecked")
  public NLBetTreeNode(final NLHand hand, final LinkedHashMap<Move, NLBetTreeNode> children,
      int index) {
    this.isRoundFirstNode = hand.getBetMoves(hand.getBetRoundIndex()).isEmpty();
    this.index = index;
    this.hand = hand;
    this.children = children;
    final List<Move> moves = new ArrayList<>();
    moves.addAll(children.keySet());
    orderedMoves = Collections.unmodifiableList(moves);
    nbChildren = orderedMoves.size();
    orderedChildren = new NLBetTreeNode[nbChildren];
    int i = 0;
    for (Move move : moves) {
      orderedChildren[i] = children.get(move);
      i++;
    }
    roundState = hand.getRoundState();
    betRoundIndex = hand.getBetRoundIndex();
    if (roundState == RoundState.END_NO_SHOWDOWN) {
      playerIndex = hand.getNoShowdownWinningPlayer();
    } else if (roundState == RoundState.WAITING_MOVE && hand.getRoundType() == RoundType.BETS) {
      playerIndex = hand.getBettingPlayer();
    } else {
      playerIndex = NO_PLAYER_INDEX;
    }
  }

  public boolean equalsForShowdown(NLBetTreeNode node) {
    if (roundState != RoundState.SHOWDOWN || node.roundState != RoundState.SHOWDOWN) {
      return false;
    }
    final List<Pot> pots = node.hand.getCurrentPots();
    final List<Pot> pots1 = hand.getCurrentPots();
    if (pots1.size() != pots.size()) {
      return false;
    }
    for (final Pot pot : pots) {
      if (!pots1.contains(pot)) {
        return false;
      }
    }
    return true;
  }

  public boolean equalsForNoShowdown(NLBetTreeNode node) {
    if (roundState != RoundState.END_NO_SHOWDOWN || node.roundState != RoundState.END_NO_SHOWDOWN) {
      return false;
    }
    if (node.playerIndex != playerIndex) {
      return false;
    }
    final Optional<List<SharedPot>> optPots = node.hand.getSharedPots();
    final Optional<List<SharedPot>> optPots1 = hand.getSharedPots();
    if (!optPots.isPresent() || !optPots1.isPresent()) {
      return false;
    }
    final List<SharedPot> pots = optPots.get();
    final List<SharedPot> pots1 = optPots1.get();
    if (pots1.size() != pots.size()) {
      return false;
    }
    for (final SharedPot pot : pots) {
      if (!pots1.contains(pot)) {
        return false;
      }
    }
    return true;
  }

  public boolean samePlayersData(NLHand hand) {
    final List<PlayerData> p1 = hand.getPlayersData();
    final List<PlayerData> p2 = this.hand.getPlayersData();
    if (p1.size() != p2.size()) {
      return false;
    }
    for (final PlayerData data : p1) {
      if (!p2.contains(data)) {
        return false;
      }
    }
    return true;
  }
}

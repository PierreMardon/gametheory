/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.NonNull;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.MoveType;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.BlindsAnteSpec;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundState;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround.BlindValue.Type;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.NoBetPlayerData;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayerData;

/**
 * Represents a blinds "round". The choice was made to call it a round as it represents a determined
 * step between ante or the hand start and the first bet round.
 *
 * @author Pierre Mardon
 *
 */
public class BlindsRound implements Cloneable {
  private final List<NoBetPlayerData> playersData;
  private final int nbPlayers;
  private final int[] bets;
  private final int[] stacks;
  private final boolean[] inHand;
  private final boolean[] shouldPostEnteringBb;
  private final boolean[] payed;
  private final int sbValue, bbValue, bbIndex, sbIndex;
  private final List<Move> seq = new LinkedList<>();
  private RoundState state = RoundState.WAITING_MOVE;

  private BlindsRound(BlindsRound src) {
    this.playersData = src.playersData;
    this.nbPlayers = src.nbPlayers;
    this.bets = src.bets.clone();
    this.stacks = src.stacks.clone();
    this.inHand = src.inHand.clone();
    this.shouldPostEnteringBb = src.shouldPostEnteringBb.clone();
    this.payed = src.payed.clone();
    this.sbValue = src.sbValue;
    this.bbValue = src.bbValue;
    this.bbIndex = src.bbIndex;
    this.sbIndex = src.sbIndex;
    this.seq.addAll(src.seq);
    this.state = src.state;
  }

  public BlindsRound(@NonNull final List<NoBetPlayerData> playersData,
      @NonNull final BlindsAnteSpec spec) {
    this.playersData = Collections.unmodifiableList(playersData);
    nbPlayers = playersData.size();
    inHand = new boolean[nbPlayers];
    stacks = new int[nbPlayers];
    bets = new int[nbPlayers];
    payed = new boolean[nbPlayers];
    shouldPostEnteringBb = new boolean[nbPlayers];
    sbValue = spec.getSbValue();
    checkArgument(sbValue >= 0, "Small blind should be >= 0");
    bbValue = spec.getBbValue();
    checkArgument(bbValue >= 0, "Big blind should be >= 0");
    int bbIndex = -1;
    int inHandPl = 0;
    int sbIndex = -1;
    for (int i = 0; i < nbPlayers; i++) {
      final NoBetPlayerData pData = playersData.get(i);
      checkArgument(pData.getStack() > 0, "In hand player %s invalid stack %s ", i,
          pData.getStack());
      if (pData.getPlayer() == spec.getBbPlayer()) {
        checkArgument(bbIndex < 0, "Multiple players considered as big blind");
        checkArgument(pData.isInHand(), "Big blind player must be in hand");
        bbIndex = i;
      }
      if (pData.getPlayer() == spec.getSbPlayer()) {
        checkArgument(sbIndex < 0, "Multiple players considered as small blind");
        checkArgument(pData.isInHand(), "Small blind player must be in hand");
        checkArgument(i != bbIndex, "One player is big AND small blind");
        sbIndex = i;
      }
      if (pData.isInHand()) {
        inHandPl++;
      }
      inHand[i] = pData.isInHand();
      stacks[i] = pData.getStack();
    }
    checkArgument(inHandPl > 0, "No player in hand");
    checkArgument(bbIndex >= 0, "No player considered as big blind");
    this.bbIndex = bbIndex;
    this.sbIndex = sbIndex;

    if (sbIndex >= 0) {
      checkArgument(sbIndex < nbPlayers, "Wrong sb index %s", sbIndex);
      checkArgument(inHand[sbIndex], "Sb %s is not in hand", sbIndex);
      checkArgument(sbIndex != bbIndex, "Sb player %s is the same as bb", sbIndex);
      int nbSbToBb = bbIndex - sbIndex;
      if (nbSbToBb < 0) {
        nbSbToBb += nbPlayers;
      }
      for (int i = 1; i < nbSbToBb; i++) {
        final int index = (sbIndex + i) % nbPlayers;
        checkArgument(inHand[index],
            "Player %s is between the small blind player and the big blind player while in the hand",
            index);
      }
    }

  }

  /**
   * Make blinds payments expire, setting out of hand all not paying players
   */
  public void expiration() {
    for (int p = 0; p < nbPlayers; p++) {
      if (inHand[p] && !payed[p] && p == sbIndex || p == bbIndex || shouldPostEnteringBb[p]) {
        inHand[p] = false;
      }
    }
    updateState();
  }

  /**
   * Get the current {@link RoundState}
   *
   * @return the current {@link RoundState}
   */
  public RoundState getState() {
    return state;
  }

  public int getNoShowdownWinningPlayer() {
    checkState(state == RoundState.END_NO_SHOWDOWN, "Wrong state %s to ask for winning player",
        state);
    int player = -1;
    for (int i = 0; i < nbPlayers; i++) {
      if (inHand[i]) {
        player = i;
        break;
      }
    }
    checkState(player >= 0, "Didn't find the winning player");
    return player;
  }

  public List<Integer> getShowdownPlayers() {
    checkState(state == RoundState.SHOWDOWN, "Wrong state %s to ask for showdown players", state);
    final List<Integer> res = new ArrayList<>();
    for (int i = 0; i < nbPlayers; i++) {
      if (inHand[i]) {
        res.add(i);
        break;
      }
    }
    return res;
  }

  private void updateState() {
    if (inHand[bbIndex] && !allBlindsPayed()) {
      return;
    }
    int nbPlNotAllIn = 0;
    int nbPl = 0;
    int lastNotAllIn = 0;
    int maxBet = 0;
    for (int p = 0; p < nbPlayers; p++) {
      if (inHand[p]) {
        maxBet = Math.max(bets[p], maxBet);
        nbPl++;
        if (stacks[p] > 0) {
          nbPlNotAllIn++;
          lastNotAllIn = p;
        }
      }
    }
    if (!inHand[bbIndex]) {
      this.state = RoundState.CANCELED;
    } else if (nbPl == 1) {
      this.state = RoundState.END_NO_SHOWDOWN;
    } else if (nbPlNotAllIn > 1) {
      this.state = RoundState.NEXT_ROUND;
    } else if (nbPlNotAllIn == 1 && bets[lastNotAllIn] < maxBet) {
      this.state = RoundState.NEXT_ROUND;
    } else {
      this.state = RoundState.SHOWDOWN;
    }
  }

  /**
   * Check if optional sb and bb players payed their blinds
   *
   * @return true when mandatory blinds were payed
   */
  public boolean mandatoryBlindsPayed() {
    if (sbIndex >= 0 && !payed[sbIndex]) {
      return false;
    }
    return payed[bbIndex];
  }

  /**
   * Check if all potential blinds payer did pay
   *
   * @return true when all in hand players payed their blinds when they have to
   */
  public boolean allBlindsPayed() {
    if (sbIndex >= 0 && !payed[sbIndex]) {
      return false;
    }
    if (!payed[bbIndex]) {
      return false;
    }
    for (int i = 0; i < nbPlayers; i++) {
      if (i != sbIndex && i != bbIndex && inHand[i] && !payed[i] && shouldPostEnteringBb[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Get the blind value for a given player that is expected to pay it
   *
   * @param player the target player
   * @return the blind value
   */
  public BlindValue getBlindValueForPlayer(int player) {
    checkArgument(inHand[player], "Player %s is not in hand", player);
    int bb = payed[player] ? bets[player] : Math.min(bbValue, stacks[player]);
    int sb = payed[player] ? bets[player] : Math.min(sbValue, stacks[player]);
    if (shouldPostEnteringBb[player] || player == bbIndex) {
      return new BlindValue(Type.BB, bb);
    }
    if (player == sbIndex) {
      return new BlindValue(Type.SB, sb);
    }
    throw new IllegalArgumentException("This player cannot pay blinds");
  }

  /**
   * Perform a blind move : pay SB, BB or refuse to pay blinds
   *
   * @param move the move to perform
   */
  public void doMove(Move move) {
    checkState(state == RoundState.WAITING_MOVE, "Current blinds round state is %s", state);
    checkNotNull(move, "Move is null");
    checkArgument(move.getType() == MoveType.BB || move.getType() == MoveType.SB
        || move.getType() == MoveType.NO_BLIND, "Wrong move type %s", move.getType());
    final Integer p = move.getPlayer();
    checkArgument(p != null, "Unknown player %s", move.getPlayer());
    checkArgument(inHand[p], "Player %s is not in hand", p);
    checkArgument(!payed[p], "Player %s already payed", p);
    switch (move.getType()) {
      case BB:
        checkArgument(bbIndex == p || shouldPostEnteringBb[p], "Player %s cannot pay big blind", p);
        checkArgument(move.getValue() == Math.min(bbValue, stacks[p]), "Wrong big blind value %s",
            move.getValue());
        payed[p] = true;
        stacks[p] -= (bets[p] = move.getValue());
        seq.add(move);
        updateState();
        return;
      case SB:
        checkArgument(sbIndex == p && !shouldPostEnteringBb[p], "Player %s cannot pay small blind",
            p);
        checkArgument(move.getValue() == Math.min(sbValue, stacks[p]), "Wrong small blind value %s",
            move.getValue());
        payed[p] = true;
        stacks[p] -= (bets[p] = move.getValue());
        seq.add(move);
        updateState();
        return;
      case NO_BLIND:
        checkArgument(bbIndex == p || sbIndex == p || shouldPostEnteringBb[p],
            "Player %s has no blinds to pay", p);
        if (p == sbIndex && !shouldPostEnteringBb[p]) {
          checkArgument(move.getValue() == Math.min(sbValue, stacks[p]),
              "Wrong value for move no blind of sb");
        } else {
          checkArgument(move.getValue() == Math.min(bbValue, stacks[p]),
              "Wrong value for move no blind");
        }
        inHand[p] = false;
        seq.add(move);
        updateState();
        return;
      default:
        throw new IllegalArgumentException("Should never happen !");
    }
  }

  /**
   * Get the list of moves performed during this blinds round
   *
   * @return the list of moves
   */
  public List<Move> getMoves() {
    return Collections.unmodifiableList(seq);
  }

  /**
   * Get the current {@link PlayerData}s
   *
   * @return the players data
   */
  public List<PlayerData> getData() {
    final List<PlayerData> res = new ArrayList<>();
    for (int i = 0; i < nbPlayers; i++) {
      boolean isInHand =
          inHand[i] && (payed[i] || (i != sbIndex && i != bbIndex && !shouldPostEnteringBb[i]));
      res.add(new PlayerData(i, stacks[i], isInHand, bets[i]));
    }

    return res;
  }

  /**
   * Get the list of players that should pay the bb to enter the hand
   *
   * @return the list of players
   */
  public List<Integer> getMissingEnteringBbPlayers() {
    List<Integer> res = new LinkedList<>();
    for (int i = 0; i < nbPlayers; i++) {
      if (inHand[i] && !payed[i] && shouldPostEnteringBb[i]) {
        res.add(i);
      }
    }
    return res;
  }

  /**
   * Check if the designated big blind player payed his blind
   *
   * @return true when the bb player payed his blind
   */
  public boolean hasBbPayed() {
    return payed[bbIndex];
  }

  /**
   * Check if the designated small blind player payed his blind
   *
   * @return true when the sb player exists and payed his blind
   */
  public boolean hasSbPayed() {
    return sbIndex >= 0 && payed[sbIndex];
  }

  @Override
  public BlindsRound clone() {
    return new BlindsRound(this);
  }
}

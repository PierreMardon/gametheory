/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.anteround;

import static com.google.common.base.Preconditions.checkArgument;
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
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.NoBetPlayerData;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayerData;

/**
 * State machine for an ante round
 *
 * @author Pierre Mardon
 *
 */
public class AnteRound implements Cloneable {
  private final List<NoBetPlayerData> playersData;
  private final int nbPlayers;
  private final int[] bets;
  private final int[] stacks;
  private final boolean[] inHand;
  private final boolean[] payed;
  private final int bbIndex;
  private final int ante;
  private final List<Move> seq = new LinkedList<>();
  private RoundState state = RoundState.WAITING_MOVE;

  public AnteRound(@NonNull final List<NoBetPlayerData> playersData,
      @NonNull final BlindsAnteSpec spec) {
    this.playersData = Collections.unmodifiableList(playersData);
    nbPlayers = playersData.size();
    inHand = new boolean[nbPlayers];
    stacks = new int[nbPlayers];
    bets = new int[nbPlayers];
    payed = new boolean[nbPlayers];
    int bbIndex = -1;
    int inHandPl = 0;
    for (int i = 0; i < nbPlayers; i++) {
      final NoBetPlayerData pData = playersData.get(i);
      checkArgument(pData.getStack() > 0, "In hand player %s invalid stack %s ", i,
          pData.getStack());
      if (pData.getPlayer() == spec.getBbPlayer()) {
        checkArgument(bbIndex < 0, "Multiple players considered as big blind");
        bbIndex = i;
      }
      if (pData.isInHand()) {
        inHandPl++;
      }
      inHand[i] = pData.isInHand();
      stacks[i] = pData.getStack();
    }
    checkArgument(inHandPl > 1, "Not enough players in hand");
    checkArgument(bbIndex >= 0, "No player considered as big blind");
    this.bbIndex = bbIndex;
    this.ante = spec.getAnteValue();
    checkArgument(ante > 0, "Ante must be > 0, found %s", ante);
  }

  private AnteRound(AnteRound src) {
    playersData = src.playersData;
    inHand = src.inHand.clone();
    stacks = src.stacks.clone();
    nbPlayers = src.nbPlayers;
    bbIndex = src.bbIndex;
    bets = src.bets.clone();
    payed = src.payed.clone();
    ante = src.ante;
    state = src.state;
    seq.addAll(src.seq);
  }

  /**
   * Get current {@link RoundState}
   *
   * @return the current {@link RoundState}
   */
  public RoundState getState() {
    return state;
  }

  /**
   * Check is the ante round is finished
   *
   * @return true when all in-hand players have payed
   */
  public boolean finished() {
    for (int i = 0; i < nbPlayers; i++) {
      if (inHand[i] && !payed[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Get the ante value a target player should or has payed
   *
   * @param player the player
   * @return the ante value
   */
  public AnteValue getAnteValueForPlayer(int player) {
    checkArgument(player >= 0, "Unknown player %s", player);
    checkArgument(inHand[player], "Player %s is not in hand", player);
    return payed[player] ? new AnteValue(bets[player])
        : new AnteValue(Math.min(ante, stacks[player]));
  }

  /**
   * Pay the ante for one player, or refuse to pay
   *
   * @param move the ante move
   */
  public void doMove(@NonNull Move move) {
    checkState(state == RoundState.WAITING_MOVE, "Current ante round state is %s", state);
    checkArgument(move.getType() == MoveType.ANTE || move.getType() == MoveType.NO_ANTE,
        "Wrong move type %s", move.getType());
    final Integer p = move.getPlayer();
    checkArgument(inHand[p], "Player %s is not in hand", p);
    checkArgument(!payed[p], "Player %s already payed", p);
    if (move.getType() == MoveType.ANTE) {
      checkArgument(move.getValue() == Math.min(ante, stacks[p]), "Wrong move value");
      payed[p] = true;
      stacks[p] -= (bets[p] = move.getValue());
    } else if (move.getType() == MoveType.NO_ANTE) {
      inHand[p] = false;
    }
    seq.add(move);
    updateState();
  }

  /**
   * Only for cash game : make players that didn't pay expire
   */
  public void expiration() {
    for (int p = 0; p < nbPlayers; p++) {
      if (inHand[p] && !payed[p]) {
        inHand[p] = false;
      }
    }
    updateState();
  }

  private void updateState() {
    if (!finished()) {
      return;
    }
    int nbPlNotAllIn = 0;
    int nbPl = 0;
    for (int p = 0; p < nbPlayers; p++) {
      if (inHand[p]) {
        nbPl++;
        if (stacks[p] > 0) {
          nbPlNotAllIn++;
        }
      }
    }
    if (nbPl < 2 || !payed[bbIndex]) {
      this.state = RoundState.CANCELED;
    } else if (nbPlNotAllIn > 1) {
      this.state = RoundState.NEXT_ROUND;
    } else {
      this.state = RoundState.SHOWDOWN;
    }
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

  /**
   * Get the current list of moves of this ante round
   *
   * @return the list of ante moves performed
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
      boolean isInHand = payed[i] && inHand[i];
      res.add(new PlayerData(i, stacks[i], isInHand, bets[i]));
    }

    return res;
  }

  /**
   * Get the current {@link NoBetPlayerData}s without bets
   *
   * @return the players data
   */
  public List<NoBetPlayerData> getNoBetData() {
    final List<NoBetPlayerData> res = new ArrayList<>();
    for (int i = 0; i < nbPlayers; i++) {
      boolean isInHand = payed[i] && inHand[i];
      res.add(new NoBetPlayerData(i, stacks[i], isInHand));
    }

    return res;
  }

  /**
   * Get the current {@link PlayerData}s with bets set to zero
   *
   * @return the players data
   */
  public List<PlayerData> getBetZeroData() {
    final List<PlayerData> res = new ArrayList<>();
    for (int i = 0; i < nbPlayers; i++) {
      boolean isInHand = payed[i] && inHand[i];
      res.add(new PlayerData(i, stacks[i], isInHand, 0));
    }

    return res;
  }

  /**
   * Get the list of players that are in hand and didn't pay their antes
   *
   * @return the list of players that are in hand and didn't pay their antes
   */
  public List<Integer> getMissingAntePlayers() {
    List<Integer> res = new LinkedList<>();
    for (int i = 0; i < nbPlayers; i++) {
      if (inHand[i] && !payed[i]) {
        res.add(i);
      }
    }
    return res;
  }

  /**
   * Get the list of players that are in hand and did pay their antes
   *
   * @return the list of players that are in hand and did pay their antes
   */
  public List<Integer> getPayedAntePlayers() {
    List<Integer> res = new LinkedList<>();
    for (int i = 0; i < nbPlayers; i++) {
      if (inHand[i] && payed[i]) {
        res.add(i);
      }
    }
    return res;
  }

  @Override
  public AnteRound clone() {
    return new AnteRound(this);
  }
}

/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.pots;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.IllegalComponentStateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.NonNull;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayerData;

/**
 * Representation of the pot and all players that contributed to it
 *
 * @author Pierre Mardon
 *
 */
public class Pot {

  /**
   * The pot's value
   */
  @Getter
  private int value;

  /**
   * Players that contributed to the pot
   */
  @Getter
  private final List<Integer> players;

  /**
   * Constructor
   *
   * @param value the value of the pot
   * @param players the contributing players
   */
  public Pot(int value, List<Integer> players) {
    this.value = value;
    this.players = Collections.unmodifiableList(players);
  }

  private void setValue(int value) {
    this.value = value;
  }

  /**
   * Get a copy of this pot for another representation of players ids
   *
   * @param players the contributing players
   * @return the new pot representation
   */
  public Pot getCopy(List<Integer> players) {
    checkArgument(this.players.size() == players.size(),
        "The new representation of contributing players has not the same number of members of the original list");
    return new Pot(value, players);
  }

  /**
   * Check if this pot is an excedent bet
   *
   * @return true when there is only one contributing player
   */
  public boolean isExcedentBet() {
    return players.size() == 1;
  }

  /**
   * Create pots from players data
   *
   * @param playersData the players data
   * @return the list of pots
   */
  public static List<Pot> getPots(@NonNull final List<PlayerData> playersData) {
    final int nbPlayers = playersData.size();
    if (nbPlayers == 0) {
      return Lists.newArrayList();
    }
    final int[] bets = new int[nbPlayers];
    final boolean[] inHand = new boolean[nbPlayers];
    for (int i = 0; i < nbPlayers; i++) {
      final PlayerData player = playersData.get(i);
      bets[i] = player.getBet();
      inHand[i] = player.isInHand();
    }
    checkArgument(nbPlayers > 1, "There must be at least two players...");
    final List<Pot> res = new LinkedList<>();
    while (true) {
      List<Integer> players = new LinkedList<>();
      int minBet = Integer.MAX_VALUE;
      for (int p = 0; p < nbPlayers; p++) {
        if (inHand[p] && bets[p] > 0) {
          players.add(p);
          minBet = Math.min(minBet, bets[p]);
        }
      }
      if (minBet == 0 || minBet == Integer.MAX_VALUE) {
        return res;
      }
      if (minBet < 0) {
        throw new IllegalComponentStateException("Min bet < 0 in pots loop");
      }
      int value = 0;
      int tmp;
      for (int p = 0; p < nbPlayers; p++) {
        tmp = Math.min(minBet, bets[p]);
        if (tmp == 0) {
          continue;
        }
        value += tmp;
        bets[p] -= tmp;
      }
      res.add(new Pot(value, players));
    }
  }

  /**
   * Create pots with players bets and in-hand data for players integer index representation, based
   * on previously created pots
   * 
   * @param lastPot
   * @param playersData
   * @return
   *
   */
  public static List<Pot> getPots(@NonNull final Pot lastPot,
      @NonNull final List<PlayerData> playersData) {
    final int nbPlayers = playersData.size();
    if (nbPlayers == 0) {
      return Lists.newArrayList();
    }
    final int[] bets = new int[nbPlayers];
    final boolean[] inHand = new boolean[nbPlayers];
    for (int i = 0; i < nbPlayers; i++) {
      final PlayerData player = playersData.get(i);
      bets[i] = player.getBet();
      inHand[i] = player.isInHand();
    }
    checkArgument(nbPlayers > 1, "There must be at least two players...");
    final List<Pot> res = new LinkedList<>();
    int[] newBets = bets.clone();
    while (true) {
      final List<Integer> players = new LinkedList<>();
      int minBet = Integer.MAX_VALUE;
      for (int p = 0; p < nbPlayers; p++) {
        if (inHand[p] && newBets[p] > 0) {
          players.add(p);
          minBet = Math.min(minBet, newBets[p]);
        }
      }
      if (minBet == 0 || minBet == Integer.MAX_VALUE) {
        return res;
      }
      if (minBet < 0) {
        throw new IllegalComponentStateException("Min bet < 0 in pots loop");
      }
      int value = 0;
      int tmp;
      for (int p = 0; p < nbPlayers; p++) {
        tmp = Math.min(minBet, newBets[p]);
        value += tmp;
        newBets[p] -= tmp;
      }
      if (res.isEmpty() && players.size() == lastPot.getPlayers().size()
          && players.containsAll(lastPot.getPlayers())) {
        lastPot.setValue(lastPot.getValue() + value);
      } else {
        res.add(new Pot(value, players));
      }
    }
  }

  @Override
  public String toString() {
    return value + " - " + Arrays.toString(players.toArray());
  }

  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof Pot)) {
      return false;
    }
    Pot p = (Pot) o;
    if (p.value != value || p.players.size() != players.size()) {
      return false;
    }
    for (Integer id : players) {
      if (!p.players.contains(id)) {
        return false;
      }
    }
    return true;
  }
}

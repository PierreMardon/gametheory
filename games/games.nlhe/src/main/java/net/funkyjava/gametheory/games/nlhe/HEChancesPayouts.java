package net.funkyjava.gametheory.games.nlhe;

import static com.google.common.base.Preconditions.checkState;

import java.util.LinkedList;
import java.util.List;

import net.funkyjava.gametheory.extensiveformgame.ChancesPayouts;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.Pot;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundState;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.NoBetPlayerData;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayerData;

/**
 * {@link ChancesPayouts} implementation for NLHE
 * 
 * @author Pierre Mardon
 *
 * @param <Chances> chances type
 */
public class HEChancesPayouts<Chances> implements ChancesPayouts<Chances> {

  private final int nbPots;
  private final int nbPlayers;
  private final double[] basePayouts;
  private final boolean[][] potsPlayers;
  private final double[] pots;
  private final int betRoundIndex;
  private final HEEquityProvider<Chances> equityProvider;

  /**
   * Constructor
   * 
   * @param hand the hand
   * @param equityProvider
   */
  public HEChancesPayouts(final NLHand hand, final HEEquityProvider<Chances> equityProvider) {
    checkState(hand.getRoundState() == RoundState.SHOWDOWN, "Hand should be in showdown state");
    this.equityProvider = equityProvider;
    final int nbPlayers = this.nbPlayers = hand.getNbPlayers();
    this.betRoundIndex = hand.getBetRoundIndex();
    final List<NoBetPlayerData> initialData = hand.getInitialPlayersData();
    final List<PlayerData> endData = hand.getPlayersData();
    final double[] basePayouts = this.basePayouts = new double[nbPlayers];
    for (int i = 0; i < nbPlayers; i++) {
      basePayouts[i] = endData.get(i).getStack() - initialData.get(i).getStack();
    }
    final List<Pot> potsList = hand.getCurrentPots();
    final List<Pot> excedentBetPots = new LinkedList<>();
    for (Pot pot : potsList) {
      if (pot.isExcedentBet()) {
        excedentBetPots.add(pot);
        final int player = pot.getPlayers().get(0);
        basePayouts[player] += pot.getValue();
      }
    }
    potsList.removeAll(excedentBetPots);
    final int nbPots = this.nbPots = potsList.size();
    final double[] pots = this.pots = new double[nbPots];
    final boolean[][] potsPlayers = this.potsPlayers = new boolean[nbPots][nbPlayers];
    final boolean[] inHand = new boolean[nbPlayers];
    for (int i = 0; i < nbPlayers; i++) {
      inHand[i] = endData.get(i).isInHand();
    }
    for (int i = 0; i < nbPots; i++) {
      final boolean[] potPlayers = potsPlayers[i];
      final Pot pot = potsList.get(i);
      pots[i] = pot.getValue();
      for (int p = 0; p < nbPlayers; p++) {
        potPlayers[p] = inHand[p] && pot.getPlayers().contains(p);
      }
    }
  }

  @Override
  public double[] getPayouts(final Chances chances) {
    final HEEquityProvider<Chances> equityProvider = this.equityProvider;
    final int nbPlayers = this.nbPlayers;
    final double[] payouts = new double[nbPlayers];
    final boolean[][] potsPlayers = this.potsPlayers;
    final double[] pots = this.pots;
    final int betRoundIndex = this.betRoundIndex;
    System.arraycopy(basePayouts, 0, payouts, 0, nbPlayers);
    final int nbPots = this.nbPots;
    for (int i = 0; i < nbPots; i++) {
      final double[] equity = equityProvider.getEquity(betRoundIndex, chances, potsPlayers[i]);
      final double pot = pots[i];
      for (int p = 0; p < nbPlayers; p++) {
        payouts[p] += equity[p] * pot;
      }
    }
    return payouts;
  }

}

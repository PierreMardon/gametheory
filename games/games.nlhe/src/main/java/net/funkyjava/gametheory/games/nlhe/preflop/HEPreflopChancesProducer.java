package net.funkyjava.gametheory.games.nlhe.preflop;

import java.util.LinkedList;
import java.util.List;

import net.funkyjava.gametheory.cscfrm.CSCFRMChancesProducer;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

public class HEPreflopChancesProducer implements CSCFRMChancesProducer<HEPreflopChances> {

  private final int nbPlayers;
  private final List<HEPreflopChances> chancesCache = new LinkedList<>();
  private final WaughIndexer preflopIndexer = new WaughIndexer(new int[] {2});
  private final int[][] allCards;
  private final int[][][] eachPlayerCardsGroups;
  private final Deck52Cards deck = new Deck52Cards(preflopIndexer.getCardsSpec());

  public HEPreflopChancesProducer(final int nbPlayers) {
    this.nbPlayers = nbPlayers;
    final int[][] allCards = this.allCards = new int[nbPlayers][2];
    final int[][][] eachPlayerCardsGroups = this.eachPlayerCardsGroups = new int[nbPlayers][1][];
    for (int i = 0; i < nbPlayers; i++) {
      eachPlayerCardsGroups[i][0] = allCards[i];
    }
  }

  @Override
  public HEPreflopChances produceChances() {
    final int[][] allCards = this.allCards;
    final int nbPlayers = this.nbPlayers;
    final List<HEPreflopChances> chancesCache = this.chancesCache;
    final WaughIndexer preflopIndexer = this.preflopIndexer;
    final int[][][] eachPlayerCardsGroups = this.eachPlayerCardsGroups;
    deck.oneShotDeckDraw(allCards);
    HEPreflopChances chances;
    int[][] playersChances;
    if (chancesCache.isEmpty()) {
      playersChances = new int[1][nbPlayers];
      chances = new HEPreflopChances(playersChances);
    } else {
      chances = chancesCache.remove(0);
      playersChances = chances.getPlayersChances();
    }
    final int[] preflopChances = playersChances[0];
    for (int i = 0; i < nbPlayers; i++) {
      preflopChances[i] = preflopIndexer.indexOf(eachPlayerCardsGroups[i]);
    }
    return chances;
  }

  @Override
  public void endedUsing(HEPreflopChances chances) {
    chancesCache.add(chances);
  }

}

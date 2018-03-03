package net.funkyjava.gametheory.games.nlhe.flop;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;
import java.util.List;

import net.funkyjava.gametheory.cscfrm.CSCFRMChancesProducer;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;


public class HEFlopChancesProducer implements CSCFRMChancesProducer<HEFlopChances> {

  private final int nbPlayers;
  private final List<HEFlopChances> chancesCache = new LinkedList<>();
  private final WaughIndexer preflopIndexer = new WaughIndexer(new int[] {2});
  private final WaughIndexer flopIndexer = new WaughIndexer(new int[] {2, 3});
  private final int[][] allCards;
  private final int[] boardCards;
  private final int[] flopCards;
  private final int[][][] eachPlayerPreflopCardsGroups;
  private final int[][][] eachPlayerFlopCardsGroups;
  private final int[] flopBuckets;
  private final Deck52Cards deck = new Deck52Cards(preflopIndexer.getCardsSpec());

  public HEFlopChancesProducer(final int nbPlayers, final int[] flopBuckets) {
    this.nbPlayers = nbPlayers;
    this.flopBuckets = checkNotNull(flopBuckets, "Flop buckets shouldn't be null");
    checkArgument(flopBuckets.length == flopIndexer.getIndexSize(),
        "Expected same size for flop buckets (" + flopBuckets.length + ") and flopIndexer ("
            + flopIndexer.getIndexSize() + ")");
    final int[][] allCards = this.allCards = new int[nbPlayers + 1][];
    final int[][][] eachPlayerPreflopCardsGroups =
        this.eachPlayerPreflopCardsGroups = new int[nbPlayers][1][];
    final int[][][] eachPlayerFlopCardsGroups =
        this.eachPlayerFlopCardsGroups = new int[nbPlayers][2][];
    final int[] boardCards = this.boardCards = new int[5];
    this.flopCards = new int[3];
    for (int i = 0; i < nbPlayers; i++) {
      eachPlayerFlopCardsGroups[i][0] =
          eachPlayerPreflopCardsGroups[i][0] = allCards[i] = new int[2];
      eachPlayerFlopCardsGroups[i][1] = boardCards;
    }
    allCards[nbPlayers] = boardCards;
  }

  @Override
  public HEFlopChances produceChances() {
    final int[][] allCards = this.allCards;
    final int nbPlayers = this.nbPlayers;
    final List<HEFlopChances> chancesCache = this.chancesCache;
    final WaughIndexer preflopIndexer = this.preflopIndexer;
    final WaughIndexer flopIndexer = this.flopIndexer;
    final int[][][] eachPlayerPreflopCardsGroups = this.eachPlayerPreflopCardsGroups;
    final int[][][] eachPlayerFlopCardsGroups = this.eachPlayerFlopCardsGroups;
    final int[] flopBuckets = this.flopBuckets;

    deck.oneShotDeckDraw(allCards);
    HEFlopChances chances;
    int[][] playersChances;
    int[][] playersCards;
    int[] flopCards;
    int[] boardCards;
    if (chancesCache.isEmpty()) {
      playersChances = new int[2][nbPlayers];
      playersCards = new int[nbPlayers][2];
      flopCards = new int[3];
      boardCards = new int[5];
      chances = new HEFlopChances(playersChances, playersCards, flopCards, boardCards);
    } else {
      chances = chancesCache.remove(0);
      playersChances = chances.getPlayersChances();
      playersCards = chances.getPlayersCards();
      flopCards = chances.getFlopCards();
      boardCards = chances.getBoardCards();
    }
    final int[] preflopChances = playersChances[0];
    for (int i = 0; i < nbPlayers; i++) {
      System.arraycopy(allCards[i], 0, playersCards[i], 0, 2);
      preflopChances[i] = preflopIndexer.indexOf(eachPlayerPreflopCardsGroups[i]);
    }
    System.arraycopy(allCards[nbPlayers], 0, boardCards, 0, 5);
    System.arraycopy(boardCards, 0, flopCards, 0, 3);
    final int[] flopChances = playersChances[1];
    for (int i = 0; i < nbPlayers; i++) {
      flopChances[i] = flopBuckets[flopIndexer.indexOf(eachPlayerFlopCardsGroups[i])];
    }

    return chances;
  }

  @Override
  public void endedUsing(HEFlopChances chances) {
    chances.setFlopEquities(null);
    chancesCache.add(chances);
  }

}

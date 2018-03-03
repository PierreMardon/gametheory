package net.funkyjava.gametheory.games.nlhe.flop;

import net.funkyjava.gametheory.games.nlhe.HEEquityProvider;
import net.funkyjava.gametheory.games.nlhe.preflop.HE3PlayersPreflopEquityProvider;
import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.ThreePlayersEquitiesIndexes;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.ThreePlayersPreflopReducedEquityTable;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

public class HE3PlayersFlopEquityProvider implements HEEquityProvider<HEFlopChances> {


  private final HE3PlayersPreflopEquityProvider preflopProvider;

  public HE3PlayersFlopEquityProvider(final ThreePlayersPreflopReducedEquityTable table) {
    this.preflopProvider = new HE3PlayersPreflopEquityProvider(table);
  }

  @Override
  public double[] getEquity(final int betRoundIndex, final HEFlopChances chances,
      boolean[] playersToConsider) {
    if (betRoundIndex == 0) {
      return preflopProvider.getEquity(betRoundIndex, chances, playersToConsider);
    }
    int index = ThreePlayersEquitiesIndexes.heroVilain1Vilain2;
    if (!playersToConsider[0]) {
      index = ThreePlayersEquitiesIndexes.vilain1Vilain2;
    } else if (!playersToConsider[1]) {
      index = ThreePlayersEquitiesIndexes.heroVilain2;
    } else if (!playersToConsider[2]) {
      index = ThreePlayersEquitiesIndexes.heroVilain1;
    }
    double[][] flopEquities = chances.getFlopEquities();
    if (flopEquities == null) {
      final TwoPlusTwoEvaluator eval = new TwoPlusTwoEvaluator();
      final IntCardsSpec specs = eval.getCardsSpec();
      final IntCardsSpec origSpecs = WaughIndexer.cardsSpecs;
      final Cards52SpecTranslator translate = new Cards52SpecTranslator(origSpecs, specs);
      final int[] evals = new int[3];
      final int[] boardCards = chances.getBoardCards();
      translate.translate(boardCards);
      final int[][] playersCards = chances.getPlayersCards();
      translate.translate(playersCards);
      eval.get7CardsEvals(playersCards, boardCards, evals);
      translate.reverse(boardCards);
      translate.reverse(playersCards);
      flopEquities = new double[4][3];
      fillEquities(flopEquities, evals[0], evals[1], evals[2]);
      chances.setFlopEquities(flopEquities);
      // final int[][] playersCards = chances.getPlayersCards();
      // final int[] flopCards = chances.getFlopCards();
      // // TODO : better than creating these objects
      // flopEquities = new ThreePlayersEquityRawEvaluator(DefaultIntCardsSpecs.getDefault(),
      // new TwoPlusTwoEvaluator()).getValues(playersCards[0], playersCards[1], playersCards[2],
      // flopCards);
      // chances.setFlopEquities(flopEquities);
    }
    return flopEquities[index];
  }

  private static void fillEquities(final double[][] equities, final int hero, final int vilain1,
      final int vilain2) {
    final boolean[] wins = {hero >= vilain1 && hero >= vilain2,
        vilain1 >= hero && vilain1 >= vilain2, vilain2 >= hero && vilain2 >= vilain1};
    final double threeEqVal =
        1 / (double) ((wins[0] ? 1 : 0) + (wins[1] ? 1 : 0) + (wins[2] ? 1 : 0));
    final double[] threeEq = equities[ThreePlayersEquitiesIndexes.heroVilain1Vilain2];
    for (int i = 0; i < 3; i++) {
      if (wins[i]) {
        threeEq[i] = threeEqVal;
      }
    }
    fillEquities(equities[ThreePlayersEquitiesIndexes.heroVilain1], 0, 1, hero, vilain1);
    fillEquities(equities[ThreePlayersEquitiesIndexes.vilain1Vilain2], 1, 2, vilain1, vilain2);
    fillEquities(equities[ThreePlayersEquitiesIndexes.heroVilain2], 0, 2, hero, vilain2);
  }

  private static void fillEquities(final double[] equities, int p1Index, int p2Index, int p1Val,
      int p2Val) {
    if (p1Val > p2Val) {
      equities[p1Index] = 1;
    } else if (p2Val > p1Val) {
      equities[p2Index] = 1;
    } else {
      equities[p1Index] = equities[p2Index] = 0.5;
    }
  }

}

package net.funkyjava.gametheory.games.nlhe.flop;

import net.funkyjava.gametheory.games.nlhe.HEEquityProvider;
import net.funkyjava.gametheory.games.nlhe.preflop.HEHUPreflopEquityProvider;
import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.HUEquityRawEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.HUPreflopEquityTables;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

public class HEHUFlopEquityProvider implements HEEquityProvider<HEFlopChances> {

  private final HEHUPreflopEquityProvider preflopProvider;
  private final HUEquityRawEvaluator equity;

  public HEHUFlopEquityProvider(final HUPreflopEquityTables tables, final IntCardsSpec specs) {
    this.preflopProvider = new HEHUPreflopEquityProvider(tables);
    equity = new HUEquityRawEvaluator(specs, new TwoPlusTwoEvaluator());
  }

  @Override
  public double[] getEquity(int betRoundIndex, HEFlopChances chances, boolean[] playersToConsider) {
    if (betRoundIndex == 0) {
      return preflopProvider.getEquity(betRoundIndex, chances, playersToConsider);
    }
    double[][] flopEquities = chances.getFlopEquities();
    if (flopEquities == null) {
      final double equity = getEquity(chances);
      flopEquities = new double[][] {{equity, 1 - equity}};
      chances.setFlopEquities(flopEquities);
    }
    return flopEquities[0];
  }

  private static final double getEquity(final HEFlopChances chances) {
    final TwoPlusTwoEvaluator eval = new TwoPlusTwoEvaluator();
    final IntCardsSpec specs = eval.getCardsSpec();
    final IntCardsSpec origSpecs = WaughIndexer.cardsSpecs;
    final Cards52SpecTranslator translate = new Cards52SpecTranslator(origSpecs, specs);
    final int[] evals = new int[2];
    final int[] boardCards = chances.getBoardCards();
    translate.translate(boardCards);
    final int[][] playersCards = chances.getPlayersCards();
    translate.translate(playersCards);
    eval.get7CardsEvals(playersCards, boardCards, evals);
    translate.reverse(boardCards);
    translate.reverse(playersCards);
    final int h = evals[0];
    final int v = evals[1];
    if (h > v) {
      return 1;
    }
    if (v > h) {
      return 0;
    }
    return 0.5;
  }

}

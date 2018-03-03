package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluator;

public class HUEquityRawEvaluator {

  private final IntCardsSpec specs;
  private final Holdem7CardsEvaluator eval;
  private final Cards52SpecTranslator specsTranslator;
  private final Deck52Cards deck;

  public HUEquityRawEvaluator(IntCardsSpec specs, Holdem7CardsEvaluator eval) {
    this.specs = specs;
    this.eval = eval;
    specsTranslator = new Cards52SpecTranslator(specs, eval.getCardsSpec());
    deck = new Deck52Cards(eval.getCardsSpec());
  }

  public double getValue(final int[] heroCards, final int[] vilainCards, final int... boardCards) {
    final Cards52SpecTranslator specsTranslator = this.specsTranslator;
    final int ca1 = heroCards[0];
    final int ca2 = heroCards[1];
    final int h1 = vilainCards[0];
    final int h2 = vilainCards[1];
    final int[] pCards =
        {specsTranslator.translate(ca1), specsTranslator.translate(ca2), 0, 0, 0, 0, 0};
    final int[] p2Cards =
        {specsTranslator.translate(h1), specsTranslator.translate(h2), 0, 0, 0, 0, 0};
    final int nbBoardCards = boardCards.length;
    final int nbPlayersKnownCards = 2 + nbBoardCards;
    final int nbBoardMissingCards = 5 - nbBoardCards;
    for (int i = 0; i < nbBoardCards; i++) {
      pCards[2 + i] = p2Cards[2 + i] = specsTranslator.translate(boardCards[i]);
    }
    final int[] usedCards = new int[4 + nbBoardCards];
    System.arraycopy(pCards, 0, usedCards, 0, 2 + nbBoardCards);
    System.arraycopy(p2Cards, 0, usedCards, 2 + nbBoardCards, 2);
    final long[] winLoseTie = new long[3];
    deck.drawAllGroupsCombinations(new int[] {nbBoardMissingCards}, (int[][] groups) -> {
      final int[] missingBoardCards = groups[0];
      System.arraycopy(missingBoardCards, 0, pCards, nbPlayersKnownCards, nbBoardMissingCards);
      System.arraycopy(missingBoardCards, 0, p2Cards, nbPlayersKnownCards, nbBoardMissingCards);
      final int eval1 = eval.get7CardsEval(pCards);
      final int eval2 = eval.get7CardsEval(p2Cards);
      if (eval1 > eval2) {
        winLoseTie[0]++;
      } else if (eval2 > eval1) {
        winLoseTie[1]++;
      } else {
        winLoseTie[2]++;
      }
      return true;
    }, usedCards);
    final double win = winLoseTie[0];
    final double lose = winLoseTie[1];
    final double tie = winLoseTie[2];

    return ((win) + (tie) / 2.0) / (win + lose + tie);
  }

  public IntCardsSpec getCardsSpec() {
    return specs;
  }

}

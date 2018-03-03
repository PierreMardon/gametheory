package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import net.funkyjava.gametheory.gameutil.cards.Cards52SpecTranslator;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluator;

public class ThreePlayersEquityRawEvaluator {


  private final IntCardsSpec specs;
  private final Holdem7CardsEvaluator eval;
  private final Cards52SpecTranslator specsTranslator;
  private final Deck52Cards deck;

  public ThreePlayersEquityRawEvaluator(IntCardsSpec specs, Holdem7CardsEvaluator eval) {
    this.specs = specs;
    this.eval = eval;
    specsTranslator = new Cards52SpecTranslator(specs, eval.getCardsSpec());
    deck = new Deck52Cards(eval.getCardsSpec());
  }

  public double[][] getValues(final int[] heroCards, final int[] vilain1Cards,
      final int[] vilain2Cards, final int... boardCards) {
    final Cards52SpecTranslator specsTranslator = this.specsTranslator;
    final int[] h7Cards = {specsTranslator.translate(heroCards[0]),
        specsTranslator.translate(heroCards[1]), 0, 0, 0, 0, 0};
    final int[] v17Cards = {specsTranslator.translate(vilain1Cards[0]),
        specsTranslator.translate(vilain1Cards[1]), 0, 0, 0, 0, 0};
    final int[] v27Cards = {specsTranslator.translate(vilain2Cards[0]),
        specsTranslator.translate(vilain2Cards[1]), 0, 0, 0, 0, 0};
    final int nbBoardCards = boardCards.length;
    final int nbPlayersKnownCards = 2 + nbBoardCards;
    final int nbBoardMissingCards = 5 - nbBoardCards;
    for (int i = 0; i < nbBoardCards; i++) {
      h7Cards[2 + i] = v17Cards[2 + i] = v27Cards[2 + i] = specsTranslator.translate(boardCards[i]);
    }
    final int[] usedCards = new int[6 + nbBoardCards];
    System.arraycopy(h7Cards, 0, usedCards, 0, 2 + nbBoardCards);
    System.arraycopy(v17Cards, 0, usedCards, 2 + nbBoardCards, 2);
    System.arraycopy(v27Cards, 0, usedCards, 4 + nbBoardCards, 2);
    final int[] heroVilain1 = new int[3];
    final int[] heroVilain2 = new int[3];
    final int[] vilain1Vilain2 = new int[3];
    final int[] threePlayers = new int[ThreePlayersHandsMatchIndexes.configurationsCount];
    deck.drawAllGroupsCombinations(new int[] {nbBoardMissingCards}, (int[][] groups) -> {
      final int[] missingBoardCards = groups[0];
      System.arraycopy(missingBoardCards, 0, h7Cards, nbPlayersKnownCards, nbBoardMissingCards);
      System.arraycopy(missingBoardCards, 0, v17Cards, nbPlayersKnownCards, nbBoardMissingCards);
      System.arraycopy(missingBoardCards, 0, v27Cards, nbPlayersKnownCards, nbBoardMissingCards);
      final int heroVal = eval.get7CardsEval(h7Cards);
      final int vilain1Val = eval.get7CardsEval(v17Cards);
      final int vilain2Val = eval.get7CardsEval(v27Cards);

      final boolean heroBeatsVilain1 = heroVal > vilain1Val;
      final boolean heroBeatsVilain2 = heroVal > vilain2Val;
      final boolean vilain1BeatsHero = vilain1Val > heroVal;
      final boolean vilain1BeatsVilain2 = vilain1Val > vilain2Val;
      final boolean vilain2BeatsHero = vilain2Val > heroVal;
      final boolean vilain2BeatsVilain1 = vilain2Val > vilain1Val;

      if (heroBeatsVilain1 && heroBeatsVilain2) {
        threePlayers[ThreePlayersHandsMatchIndexes.heroWins]++;
      } else if (vilain2BeatsHero && vilain2BeatsVilain1) {
        threePlayers[ThreePlayersHandsMatchIndexes.vilain2Wins]++;
      } else if (vilain1BeatsHero && vilain1BeatsVilain2) {
        threePlayers[ThreePlayersHandsMatchIndexes.vilain1Wins]++;
      } else {
        // we have an equality, three players will tie
        if (heroVal == vilain1Val && heroVal == vilain2Val) {
          threePlayers[ThreePlayersHandsMatchIndexes.split3Players]++;
        } else {
          // Nope, two players will tie, one will lose
          if (heroVal == vilain1Val) {
            threePlayers[ThreePlayersHandsMatchIndexes.splitHeroVilain1]++;
          } else if (heroVal == vilain2Val) {
            threePlayers[ThreePlayersHandsMatchIndexes.splitHeroVilain2]++;
          } else {
            threePlayers[ThreePlayersHandsMatchIndexes.splitVilain1Vilain2]++;
          }
        }
      }
      if (heroBeatsVilain1) {
        heroVilain1[0]++;
      } else if (vilain1BeatsHero) {
        heroVilain1[1]++;
      } else {
        heroVilain1[2]++;
      }
      if (heroBeatsVilain2) {
        heroVilain2[0]++;
      } else if (vilain2BeatsHero) {
        heroVilain2[1]++;
      } else {
        heroVilain2[2]++;
      }
      if (vilain1BeatsVilain2) {
        vilain1Vilain2[0]++;
      } else if (vilain2BeatsVilain1) {
        vilain1Vilain2[1]++;
      } else {
        vilain1Vilain2[2]++;
      }
      return true;
    }, usedCards);
    final double[][] handEquities = new double[4][3];
    final double[] threePlayersEq = handEquities[ThreePlayersEquitiesIndexes.heroVilain1Vilain2];
    double threeTotal = 0;
    threeTotal += threePlayersEq[0] = threePlayers[ThreePlayersHandsMatchIndexes.heroWins]
        + threePlayers[ThreePlayersHandsMatchIndexes.split3Players] / 3d
        + (threePlayers[ThreePlayersHandsMatchIndexes.splitHeroVilain1]
            + threePlayers[ThreePlayersHandsMatchIndexes.splitHeroVilain2]) / 2d;
    threeTotal += threePlayersEq[1] = threePlayers[ThreePlayersHandsMatchIndexes.vilain1Wins]
        + threePlayers[ThreePlayersHandsMatchIndexes.split3Players] / 3d
        + (threePlayers[ThreePlayersHandsMatchIndexes.splitHeroVilain1]
            + threePlayers[ThreePlayersHandsMatchIndexes.splitVilain1Vilain2]) / 2d;
    threeTotal += threePlayersEq[2] = threePlayers[ThreePlayersHandsMatchIndexes.vilain2Wins]
        + threePlayers[ThreePlayersHandsMatchIndexes.split3Players] / 3d
        + (threePlayers[ThreePlayersHandsMatchIndexes.splitHeroVilain2]
            + threePlayers[ThreePlayersHandsMatchIndexes.splitVilain1Vilain2]) / 2d;
    threePlayersEq[0] /= threeTotal;
    threePlayersEq[1] /= threeTotal;
    threePlayersEq[2] /= threeTotal;
    final double[] vilain2FoldsEq = handEquities[ThreePlayersEquitiesIndexes.heroVilain1];
    final int hV1Win = heroVilain1[0];
    final int hV1Lose = heroVilain1[1];
    final int hV1Tie = heroVilain1[2];
    vilain2FoldsEq[1] =
        1 - (vilain2FoldsEq[0] = (hV1Win + hV1Tie / 2d) / (hV1Win + hV1Tie + hV1Lose));

    final double[] vilain1FoldsEq = handEquities[ThreePlayersEquitiesIndexes.heroVilain2];
    final int hV2Win = heroVilain2[0];
    final int hV2Lose = heroVilain2[1];
    final int hV2Tie = heroVilain2[2];
    vilain1FoldsEq[2] =
        1 - (vilain1FoldsEq[0] = (hV2Win + hV2Tie / 2d) / (hV2Win + hV2Tie + hV2Lose));

    final double[] heroFoldsEq = handEquities[ThreePlayersEquitiesIndexes.vilain1Vilain2];
    final int v1V2Win = vilain1Vilain2[0];
    final int v1V2Lose = vilain1Vilain2[1];
    final int v1V2Tie = vilain1Vilain2[2];
    heroFoldsEq[2] =
        1 - (heroFoldsEq[1] = (v1V2Win + v1V2Tie / 2d) / (v1V2Win + v1V2Tie + v1V2Lose));
    return handEquities;
  }

  public IntCardsSpec getCardsSpec() {
    return specs;
  }

}

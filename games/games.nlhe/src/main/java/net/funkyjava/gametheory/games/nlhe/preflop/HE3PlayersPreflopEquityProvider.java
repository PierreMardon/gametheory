package net.funkyjava.gametheory.games.nlhe.preflop;

import net.funkyjava.gametheory.games.nlhe.HEEquityProvider;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.ThreePlayersEquitiesIndexes;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.ThreePlayersPreflopReducedEquityTable;

public class HE3PlayersPreflopEquityProvider implements HEEquityProvider<HEPreflopChances> {

  private final ThreePlayersPreflopReducedEquityTable table;

  public HE3PlayersPreflopEquityProvider(final ThreePlayersPreflopReducedEquityTable table) {
    this.table = table;
  }

  @Override
  public double[] getEquity(final int betRoundIndex, final HEPreflopChances chances,
      boolean[] playersToConsider) {
    int index = ThreePlayersEquitiesIndexes.heroVilain1Vilain2;
    if (!playersToConsider[0]) {
      index = ThreePlayersEquitiesIndexes.vilain1Vilain2;
    } else if (!playersToConsider[1]) {
      index = ThreePlayersEquitiesIndexes.heroVilain2;
    } else if (!playersToConsider[2]) {
      index = ThreePlayersEquitiesIndexes.heroVilain1;
    }
    final int[] pChances = chances.getPlayersChances()[0];
    return table.getEquities(pChances[0], pChances[1], pChances[2])[index];
  }

}

package net.funkyjava.gametheory.games.nlhe;

/**
 * Interface to provide stake independent equity
 * 
 * @author Pierre Mardon
 *
 * @param <Chances> the chances type
 */
public interface HEEquityProvider<Chances> {

  /**
   * Get equity
   * 
   * @param betRoundIndex the bet round index
   * @param chances the chances
   * @param playersToConsider player that are still in hand
   * @return the equity
   */
  double[] getEquity(final int betRoundIndex, final Chances chances,
      final boolean[] playersToConsider);

}

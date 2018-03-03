package net.funkyjava.gametheory.extensiveformgame;

/**
 * Interface to access the utility of a chance dependent terminal node. Every implementation is
 * expected to be thread safe
 * 
 * @author Pierre Mardon
 *
 * @param <Chances> the chances type
 */
public interface ChancesPayouts<Chances> {

  /**
   * Returns the payouts given the chances
   * 
   * @param chances the chances
   * @return the payouts
   */
  double[] getPayouts(final Chances chances);
}

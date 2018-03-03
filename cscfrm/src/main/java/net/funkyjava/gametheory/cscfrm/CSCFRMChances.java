package net.funkyjava.gametheory.cscfrm;

/**
 * Intends to store generated chances.
 * 
 * 
 * @author Pierre Mardon
 *
 */
public interface CSCFRMChances {

  /**
   * Get the stored chances : first index is the round, second the player's one
   * 
   * @return the stored chances
   */
  int[][] getPlayersChances();

}

package net.funkyjava.gametheory.cscfrm;

/**
 * 
 * Random drawer for CSCFRM.
 * 
 * @author Pierre Mardon
 *
 * @param <Chances> the chances class used
 */
public interface CSCFRMChancesProducer<Chances extends CSCFRMChances> {

  /**
   * Draw random chances
   * 
   * @return the drawn chances
   */
  Chances produceChances();

  /**
   * CSCFRM algorithm stopped using these produce chances. They can be recycled
   * 
   * @param chances
   */
  void endedUsing(Chances chances);
}

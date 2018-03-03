package net.funkyjava.gametheory.cscfrm;

import java.util.List;

/**
 * Chances synchronizers are responsible for producing chances. They can require to run on a
 * parallel thread in which case they must provide producer runners via the {@link #getProducers()}
 * method. They should return null for a {@link #getChances()} call if it is called after the
 * {@link #stop()} method.
 * 
 * Implementations must be thread safe.
 * 
 * @author Pierre Mardon
 *
 * @param <Chances>
 */
public interface CSCFRMChancesSynchronizer<Chances extends CSCFRMChances> {

  /**
   * Get produced chances
   * 
   * @return the produced chances
   * @throws InterruptedException
   */
  Chances getChances() throws InterruptedException;

  /**
   * 
   * The CSCFRM algorithm ended using the previously produced chances, they can be recycled.
   * 
   * @param chances
   * @throws InterruptedException
   */
  void endUsing(final Chances chances) throws InterruptedException;

  /**
   * CSCFRM algorithm will stop running, stop producing chances, stop runnables provided by
   * {@link #getProducers()}.
   */
  void stop();

  /**
   * Everything can be reset to potentially start a new chances production session
   */
  void reset();

  /**
   * The list - potentially empty - of runnables required to produce chances. They will be run by
   * the main CSCFRM algorithm executor.
   * 
   * @return the producers
   */
  List<Runnable> getProducers();
}

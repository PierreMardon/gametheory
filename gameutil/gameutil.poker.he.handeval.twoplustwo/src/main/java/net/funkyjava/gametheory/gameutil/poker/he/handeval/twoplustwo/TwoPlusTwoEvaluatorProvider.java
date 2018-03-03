/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo;

import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem5CardsEvaluatorProvider;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem6CardsEvaluatorProvider;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluatorProvider;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.HoldemFullEvaluatorProvider;

/**
 * Provider for {@link TwoPlusTwoEvaluator}. As this evaluator is thread safe, we provide always the
 * same instance.
 *
 * @author Pierre Mardon
 *
 */
public class TwoPlusTwoEvaluatorProvider implements HoldemFullEvaluatorProvider,
    Holdem7CardsEvaluatorProvider, Holdem6CardsEvaluatorProvider, Holdem5CardsEvaluatorProvider {

  private static Object lock = new Object();
  private static TwoPlusTwoEvaluator eval = null;

  /**
   * The constructor
   */
  public TwoPlusTwoEvaluatorProvider() {
    synchronized (lock) {
      if (eval == null) {
        eval = new TwoPlusTwoEvaluator();
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see net.funkyjava.cscfrm.game.poker.he.handeval.itf.HoldemEvaluatorProvider #getEvaluator()
   */
  @Override
  public TwoPlusTwoEvaluator getEvaluator() {
    return eval;
  }

}

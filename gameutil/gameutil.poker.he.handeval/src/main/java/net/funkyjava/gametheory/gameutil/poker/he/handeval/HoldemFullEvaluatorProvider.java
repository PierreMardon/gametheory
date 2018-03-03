/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.he.handeval;

/**
 * Provider for {@link HoldemFullEvaluator}
 *
 * @author Pierre Mardon
 *
 */
public interface HoldemFullEvaluatorProvider extends Holdem5CardsEvaluatorProvider,
    Holdem6CardsEvaluatorProvider, Holdem7CardsEvaluatorProvider {

  /**
   * Gets an evaluator. When the evaluator implementation is not thread-safe, should create a new
   * one for each call.
   *
   * @return the evaluator
   */
  @Override
  HoldemFullEvaluator getEvaluator();
}

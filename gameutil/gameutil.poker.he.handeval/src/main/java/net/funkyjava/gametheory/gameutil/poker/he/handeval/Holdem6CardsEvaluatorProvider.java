/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.he.handeval;

/**
 * Provider for {@link Holdem7CardsEvaluator}
 *
 * @author Pierre Mardon
 *
 */
public interface Holdem6CardsEvaluatorProvider {

  /**
   * Gets an evaluator. When the evaluator implementation is not thread-safe, should create a new
   * one for each call.
   *
   * @return the evaluator
   */
  Holdem6CardsEvaluator getEvaluator();
}

/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem5CardsEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem6CardsEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.Holdem7CardsEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.HoldemFullEvaluator;

/**
 * 2+2 hand evaluator.
 *
 * @author Pierre Mardon
 *
 */
public class TwoPlusTwoEvaluator implements HoldemFullEvaluator, Holdem5CardsEvaluator,
    Holdem6CardsEvaluator, Holdem7CardsEvaluator {

  private int currentIndex;

  private static final IntCardsSpec spec = new IntCardsSpec() {

    @Override
    public int getStandardRank(int card) {
      return (card - 1) / 4;
    }

    @Override
    public int getStandardColor(int card) {
      return (card - 1) % 4;
    }

    @Override
    public int getOffset() {
      return 1;
    }

    @Override
    public boolean sameColor(int card1, int card2) {
      return (card1 - 1) % 4 == (card2 - 1) % 4;
    }

    @Override
    public boolean sameRank(int card1, int card2) {
      return (card1 - 1) / 4 == (card2 - 1) / 4;
    }

    @Override
    public int getCard(int stdRank, int stdColor) {
      return stdColor + 4 * stdRank + 1;
    }
  };

  /**
   * The constructor. Will generate tables if not already done.
   */
  public TwoPlusTwoEvaluator() {
    Generator.generateTables();
  }

  @Override
  public int compare7CardsHands(int[] h1, int[] h2, int[] board) {
    int b;
    final int[] handRanks = Generator.handRanks;
    return handRanks[handRanks[(b =
        handRanks[handRanks[handRanks[handRanks[handRanks[53 + board[0]] + board[1]] + board[2]]
            + board[3]] + board[4]])
        + h1[0]] + h1[1]] - handRanks[handRanks[b + h2[0]] + h2[1]];

  }

  @Override
  public int get7CardsEval(int[] hand) {
    final int[] handRanks = Generator.handRanks;
    return handRanks[handRanks[handRanks[handRanks[handRanks[handRanks[handRanks[53 + hand[0]]
        + hand[1]] + hand[2]] + hand[3]] + hand[4]] + hand[5]] + hand[6]];
  }

  @Override
  public void get7CardsEvals(int[][] hands, int[] board, int[] dest) {
    final int[] handRanks = Generator.handRanks;
    int b = handRanks[handRanks[handRanks[handRanks[handRanks[53 + board[0]] + board[1]] + board[2]]
        + board[3]] + board[4]];
    for (int i = 0; i < hands.length; i++) {
      dest[i] = handRanks[handRanks[b + hands[i][0]] + hands[i][1]];
    }
  }

  @Override
  public int compare5CardsHands(int[] h1, int[] h2, int[] board) {
    int b;
    final int[] handRanks = Generator.handRanks;
    return handRanks[handRanks[(b =
        handRanks[handRanks[handRanks[53 + board[0]] + board[1]] + board[2]]) + h1[0]] + h1[1]]
        - handRanks[handRanks[b + h2[0]] + h2[1]];
  }

  @Override
  public int get5CardsEval(int[] hand) {
    final int[] handRanks = Generator.handRanks;
    return handRanks[handRanks[handRanks[handRanks[handRanks[53 + hand[0]] + hand[1]] + hand[2]]
        + hand[3]] + hand[4]];
  }

  @Override
  public void get5CardsEvals(int[][] hands, int[] board, int[] dest) {
    final int[] handRanks = Generator.handRanks;
    int b = handRanks[handRanks[handRanks[53 + board[0]] + board[1]] + board[2]];
    for (int i = 0; i < hands.length; i++) {
      dest[i] = handRanks[handRanks[b + hands[i][0]] + hands[i][1]];
    }
  }

  @Override
  public int compare6CardsHands(int[] h1, int[] h2, int[] board) {
    int b;
    final int[] handRanks = Generator.handRanks;
    return handRanks[handRanks[(b =
        handRanks[handRanks[handRanks[handRanks[53 + board[0]] + board[1]] + board[2]] + board[3]])
        + h1[0]] + h1[1]] - handRanks[handRanks[b + h2[0]] + h2[1]];
  }

  @Override
  public int get6CardsEval(int[] hand) {
    final int[] handRanks = Generator.handRanks;
    return handRanks[handRanks[handRanks[handRanks[handRanks[handRanks[53 + hand[0]] + hand[1]]
        + hand[2]] + hand[3]] + hand[4]] + hand[5]];
  }

  @Override
  public void get6CardsEvals(int[][] hands, int[] board, int[] dest) {
    final int[] handRanks = Generator.handRanks;
    int b =
        handRanks[handRanks[handRanks[handRanks[53 + board[0]] + board[1]] + board[2]] + board[3]];
    for (int i = 0; i < hands.length; i++) {
      dest[i] = handRanks[handRanks[b + hands[i][0]] + hands[i][1]];
    }

  }

  public final void setFirstCards(final int[] cards) {
    final int[] handRanks = Generator.handRanks;
    int index = handRanks[53 + cards[0]];
    final int length = cards.length;
    for (int i = 1; i < length; i++) {
      index = handRanks[index + cards[i]];
    }
    currentIndex = index;
  }

  public final int getEvalForNextCards(final int[] cards) {
    final int[] handRanks = Generator.handRanks;
    int index = currentIndex;
    final int length = cards.length;
    for (int i = 0; i < length; i++) {
      index = handRanks[index + cards[i]];
    }
    return index;
  }

  public final int getEvalForNext2Cards(final int[] cards) {
    final int[] handRanks = Generator.handRanks;
    return handRanks[handRanks[cards[0] + currentIndex] + cards[1]];
  }

  /*
   * (non-Javadoc)
   *
   * @see net.funkyjava.cscfrm.game.poker.he.handeval.itf.HoldemEvaluator# getCardsSpec ()
   */
  @Override
  public IntCardsSpec getCardsSpec() {
    return spec;
  }

}

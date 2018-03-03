package net.funkyjava.gametheory.games.nlhe.flop;

import net.funkyjava.gametheory.games.nlhe.preflop.HEPreflopChances;

public class HEFlopChances extends HEPreflopChances {

  private double[][] flopEquities;

  private int[][] playersCards;

  private int[] flopCards;
  private int[] boardCards;

  public HEFlopChances(final int[][] playersChances, final int[][] playersCards,
      final int[] flopCards, final int[] boardCards) {
    super(playersChances);
    this.playersCards = playersCards;
    this.flopCards = flopCards;
    this.boardCards = boardCards;
  }

  public double[][] getFlopEquities() {
    return flopEquities;
  }

  public void setFlopEquities(double[][] flopEquities) {
    this.flopEquities = flopEquities;
  }

  public int[][] getPlayersCards() {
    return playersCards;
  }

  public int[] getFlopCards() {
    return flopCards;
  }

  public int[] getBoardCards() {
    return boardCards;
  }

}

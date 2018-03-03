package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

import java.util.Arrays;

import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.CardsGroupsIndexer;

abstract public class Indexer implements CardsGroupsIndexer {
  // number of entries done
  public int count = 0;
  // whether to give out extra debug info
  public boolean generationDebug = false;

  public int numCards = -1;

  private final int tableSize;

  // The different suit patterns matching to each rank pattern. [number of
  // rank patterns]
  public Suits rankPatternSuits[];
  // how many ranks that compress into a certain rank pattern [number of rank
  // patterns]
  public int[] numRankPattern;
  // Gives each rank a unique position within the rank pattern it belongs to
  // [number of ranks]
  public int[] rankPositionMap;
  // Says to which rank pattern index this rank belongs to [number of ranks]
  public int[] rankIndexMap;
  // The current smallest un-used rank pattern index
  public int rankPatternCount = 0;
  // Cumulative sum of offsets (see tableIndex)
  private int cumOffsets[];

  private static DjhemligCardsSpec cardsSpec = new DjhemligCardsSpec();

  protected Indexer(int tableSize) {
    this.tableSize = tableSize;
  }

  /*
   * Implemented by [street]tables
   */
  abstract public int handRankIndex(int Rank[]);

  // sort hole cards, sort board cards and put into Rank and Suit
  private static void getSortedRankSuits(int cards[][], int Rank[], int Suit[]) {
    Arrays.sort(cards[0]);
    Arrays.sort(cards[1]);

    for (int i = 0; i < 2; i++) {
      Rank[i] = cards[0][i] / 4;
      Suit[i] = cards[0][i] % 4;
    }

    for (int i = 0; i < cards[1].length; i++) {
      Rank[i + 2] = cards[1][i] / 4;
      Suit[i + 2] = cards[1][i] % 4;
    }
  }

  /*
   * Entry for looking up a hand. First two cards are hole rest board.
   */
  @Override
  public int indexOf(int cardsGroups[][]) {
    int length = 0;
    for (int[] cardsGroup : cardsGroups) {
      length += cardsGroup.length;
    }
    final int Rank[] = new int[length];
    final int Suit[] = new int[length];
    getSortedRankSuits(cardsGroups, Rank, Suit);
    return tableIndex(Rank, Suit);

  }

  /*
   * Build offset table
   */
  private void countOffsets() {
    cumOffsets = new int[numRankPattern.length];

    for (int i = 1; i < numRankPattern.length; i++) {
      cumOffsets[i] = cumOffsets[i - 1] + numRankPattern[i - 1] * rankPatternSuits[i - 1].getSize();
    }
  }

  private int tableIndex(int Rank[], int Suit[]) {
    int rankidx = handRankIndex(Rank);
    int rankIsoIndex = rankIndexMap[rankidx];
    int suitIndex = rankPatternSuits[rankIsoIndex].getPatternIndex(Suit);

    // useful for debugging.
    // int suitPattern [] =
    // rankPatternSuits[rankIsoIndex].getPattern(suitIndex);
    // Helper.printArray("suitPattern", suitPattern);
    // System.out.println("suitIndex" + suitIndex);

    int index = cumOffsets[rankIsoIndex]
        + rankPositionMap[rankidx] * rankPatternSuits[rankIsoIndex].getSize() + suitIndex;

    return index;
  }

  public void initialize() {
    countOffsets();
    System.gc();
  }

  public void debugInfo() {
    int s = 0;

    for (int i = 0; i < rankPatternCount; i++) {
      s += numRankPattern[i] * rankPatternSuits[i].getSize();
    }

    long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    System.out.println("Mem used:" + mem0 / 1048576 + " MB");

    System.out.println("Size:" + s);
  }

  /*
   * Returns the isomorphic lowest rank pattern.
   */
  public int[] lowestRank(int ranks[]) {
    int map[] = new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    int isoRank[] = new int[ranks.length];

    int currentrank = 0;

    for (int i = 0; i < ranks.length; i++) {
      if (map[ranks[i]] == -1) {
        map[ranks[i]] = currentrank;
        currentrank++;
      }
      isoRank[i] = map[ranks[i]];
    }
    return isoRank;
  }

  /*
   * 0..90
   */
  private static final int[][] holeRankIndex =
      new int[][] {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
          {1, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24},
          {2, 14, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35},
          {3, 15, 26, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45},
          {4, 16, 27, 37, 46, 47, 48, 49, 50, 51, 52, 53, 54},
          {5, 17, 28, 38, 47, 55, 56, 57, 58, 59, 60, 61, 62},
          {6, 18, 29, 39, 48, 56, 63, 64, 65, 66, 67, 68, 69},
          {7, 19, 30, 40, 49, 57, 64, 70, 71, 72, 73, 74, 75},
          {8, 20, 31, 41, 50, 58, 65, 71, 76, 77, 78, 79, 80},
          {9, 21, 32, 42, 51, 59, 66, 72, 77, 81, 82, 83, 84},
          {10, 22, 33, 43, 52, 60, 67, 73, 78, 82, 85, 86, 87},
          {11, 23, 34, 44, 53, 61, 68, 74, 79, 83, 86, 88, 89},
          {12, 24, 35, 45, 54, 62, 69, 75, 80, 84, 87, 89, 90}};

  /*
   * Hole rank index.
   */
  public int holeRankIndex(int hRank[]) {
    return holeRankIndex[hRank[0]][hRank[1]];
  }

  @Override
  public int getIndexSize() {
    return tableSize;
  }

  @Override
  public IntCardsSpec getCardsSpec() {
    return cardsSpec;
  }

}

package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

import java.util.Arrays;

abstract public class Table {
  // dryrun=1 means only counting, dryrun=0 generates actual tables.
  public int dryrun = 1;
  // number of entries done
  public int count = 0;
  // whether to give out extra debug info
  public boolean generationDebug = false;

  public int numCards = -1;
  public float LUT[];

  // The different suit patterns matching to each rank pattern. [number of rank patterns]
  public Suits rankPatternSuits[];
  // how many ranks that compress into a certain rank pattern [number of rank patterns]
  public int[] numRankPattern;
  // Gives each rank a unique position within the rank pattern it belongs to [number of ranks]
  public int[] rankPositionMap;
  // Says to which rank pattern index this rank belongs to [number of ranks]
  public int[] rankIndexMap;
  // The current smallest un-used rank pattern index
  public int rankPatternCount = 0;
  // Cumulative sum of offsets (see tableIndex)
  private int cumOffsets[];

  /*
   * Implemented by [street]tables
   */
  abstract public int handRankIndex(int Rank[]);

  abstract public float handEval(int cards[]);

  abstract public void initializeTable();

  // sort hole cards, sort board cards and put into Rank and Suit
  private static void getSortedRankSuits(int cards[], int Rank[], int Suit[]) {
    int hole[] = new int[] {cards[0], cards[1]};
    int board[] = new int[cards.length - 2];

    System.arraycopy(cards, 2, board, 0, cards.length - 2);
    Arrays.sort(board);
    Arrays.sort(hole);

    for (int i = 0; i < 2; i++) {
      Rank[i] = hole[i] / 4;
      Suit[i] = hole[i] % 4;
    }

    for (int i = 2; i < cards.length; i++) {
      Rank[i] = board[i - 2] / 4;
      Suit[i] = board[i - 2] % 4;
    }
  }

  /*
   * Entry for looking up a hand. First two cards are hole rest board.
   */
  public float lookupOne(int cards[]) {
    int Rank[] = new int[cards.length];
    int Suit[] = new int[cards.length];

    getSortedRankSuits(cards, Rank, Suit);
    // Helper.printArray("Rank", Rank);
    // Helper.printArray("Suit", suits);

    int finalindex = tableIndex(Rank, Suit);

    return LUT[finalindex];
  }

  /*
   * Generates a sorted deck except for SORTED cards in deadcards.
   */
  private static int[] getDeck(int deadcards[]) {
    int ndeadcards = deadcards.length;
    int di = 0;
    int deck[] = new int[52 - ndeadcards];

    Arrays.sort(deadcards);

    for (int c = 0, idx = 0; c < 52; c++) {
      if (di < ndeadcards && c == deadcards[di]) {
        di++;
        continue;
      }

      deck[idx] = c;
      idx++;
    }

    return deck;
  }

  /*
   * Index of the hand into [0, nchoosek(52,2)-1] see http://en.wikipedia.org/wiki/Combinadic hole1
   * < hole2
   */
  private static int holeIndex(int hole1, int hole2) {
    return Helper.nchoosek(hole2, 2) + Helper.nchoosek(hole1, 1);
  }

  /*
   * Looks up all hole card measures for a certain board
   */
  public float[] lookupAll(int board[]) {
    int ncards = board.length + 2;
    int Rank[] = new int[ncards];
    int Suit[] = new int[ncards];
    float values[] = new float[1326];

    // -1 for impossible hole cards
    for (int i = 0; i < values.length; i++) {
      values[i] = -1;
    }

    Arrays.sort(board);
    for (int i = 0; i < board.length; i++) {
      Rank[i + 2] = board[i] / 4;
      Suit[i + 2] = board[i] % 4;
    }

    int deck[] = getDeck(board);
    for (int i = 0; i < deck.length; i++) {
      for (int j = i + 1; j < deck.length; j++) {
        Rank[0] = deck[i] / 4;
        Suit[0] = deck[i] % 4;
        Rank[1] = deck[j] / 4;
        Suit[1] = deck[j] % 4;

        int holeindex = holeIndex(deck[i], deck[j]);

        values[holeindex] = LUT[tableIndex(Rank, Suit)];
      }
    }

    return values;
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
    // int suitPattern [] = rankPatternSuits[rankIsoIndex].getPattern(suitIndex);
    // Helper.printArray("suitPattern", suitPattern);
    // System.out.println("suitIndex" + suitIndex);

    int index = cumOffsets[rankIsoIndex]
        + rankPositionMap[rankidx] * rankPatternSuits[rankIsoIndex].getSize() + suitIndex;

    return index;
  }

  /*
   * Common for each generation of [street]Table
   */
  private void fillTable(int[] Rank, int suitpattern[]) {
    int idx = tableIndex(Rank, suitpattern);

    // Generates one hand
    int cards[] = Helper.getHand(Rank, suitpattern);

    // some info of how it's doing since it takes a very long time.
    count++;

    if (count % 10000 == 0) {
      System.out.println("Finished " + count);
    }

    LUT[idx] = handEval(cards);
  }

  /*
   * Common for each generation of [street]Table
   */
  public void enumerateSuits(int[] Rank) {
    int rankidx = handRankIndex(Rank);

    // index of this rank pattern
    int rankIsoIndex = rankIndexMap[rankidx];

    // These are the suit patterns belonging to this rank pattern
    Suits f = rankPatternSuits[rankIsoIndex];

    // For each suit pattern belonging to this rank pattern
    for (int i = 0; i < f.getSize(); i++) {
      fillTable(Rank, f.getPattern(i));
    }
  }

  public void initialize() {
    initializeTable();
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
   * Tests the lookup table wrt. the measure function
   */
  public int testLUT(int n) {
    int tested = 0;

    for (int i = 0; i < n; i++) {
      int cards[] = Helper.randomHand(numCards);
      float lookup = lookupOne(cards);

      if (lookup <= 0) {
        continue;
      }

      float direct = handEval(cards);

      if (direct < 0) {
        continue;
      }

      if (lookup != direct) {
        Helper.printArray("Bad lookup in the LUT:", cards);
        System.out.println("direct:" + direct + " and lookupOne:" + lookup);
      }
      tested++;
    }
    return tested;
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
}

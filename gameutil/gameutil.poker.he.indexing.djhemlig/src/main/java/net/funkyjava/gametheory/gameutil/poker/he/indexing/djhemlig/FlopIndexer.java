package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

// import measure.*;

public class FlopIndexer extends Indexer {
  // Says to which rank pattern this rank belongs.
  private int[][][][][] rankPatternIndex = new int[5][5][5][5][5];

  public static final int[] bRank1 = {0, 12, 23, 33, 42, 50, 57, 63, 68, 72, 75, 77, 78};
  public static final int[] bRank2 = {0, 78, 144, 199, 244, 280, 308, 329, 344, 354, 360, 363, 364};

  public FlopIndexer() {
    super(1361802);
    numCards = 5;
    rankPatternSuits = new FlopSuit[36];
    numRankPattern = new int[36];
    rankPositionMap = new int[41405];
    rankIndexMap = new int[41405];
    int sizev[] = new int[] {5, 5, 5, 5, 5};

    // Set all rankPatternIndex entries to -1
    Helper.init_int5(rankPatternIndex, sizev, -1);
  }

  /*
   * Rank index of the board [0, 454]
   */
  public static int boardRankIndex(int bRank[]) {
    return bRank2[bRank[0]] + bRank1[bRank[1]] + bRank[2];
  }

  /*
   * Creates index for every rank (hole rank, board rank) combination.
   */
  @Override
  public int handRankIndex(int Rank[]) {
    int hRank[] = new int[] {Rank[0], Rank[1]};
    int bRank[] = new int[] {Rank[2], Rank[3], Rank[4]};

    int hridx = holeRankIndex(hRank);
    int bridx = boardRankIndex(bRank);

    return bridx * 91 + hridx;
  }

  /*
   * Only used when doing a dry run to count and generate indexing tables.
   */
  private void countRankSuits(int[] Rank) {
    int r[] = lowestRank(Rank);
    int rankIsoIndex = rankPatternIndex[r[0]][r[1]][r[2]][r[3]][r[4]];

    // Haven't come upon this rank pattern yet, add it.
    if (rankIsoIndex == -1) {
      rankPatternSuits[rankPatternCount] = new FlopSuit();
      Suits f = rankPatternSuits[rankPatternCount];
      rankPatternIndex[r[0]][r[1]][r[2]][r[3]][r[4]] = rankPatternCount;

      f.enumSuits(r);

      if (generationDebug) {
        Helper.printArray("", r);
        System.out.println("Num suits:" + f.getSize() + " rankindex:" + rankPatternCount);
      }

      rankIsoIndex = rankPatternCount;
      rankPatternCount++;
    }

    int rankidx = handRankIndex(Rank);

    rankPositionMap[rankidx] = numRankPattern[rankIsoIndex];
    rankIndexMap[rankidx] = rankIsoIndex;

    numRankPattern[rankIsoIndex]++;
  }

  private void enumerateBoard(int[] Rank) {
    for (int k = 0; k < 13; k++) {
      for (int l = k; l < 13; l++) {
        for (int m = l; m < 13; m++) {
          Rank[2] = k;
          Rank[3] = l;
          Rank[4] = m;

          // no 5 of a kind please
          if (Rank[0] == Rank[1] && Rank[1] == Rank[2] && Rank[2] == Rank[4]) {
            continue;
          }

          countRankSuits(Rank);
        }
      }
    }
  }

  private void enumerateHole() {
    int[] Rank = new int[5];

    for (int i = 0; i < 13; i++) {
      for (int j = i; j < 13; j++) {
        Rank[0] = i;
        Rank[1] = j;

        enumerateBoard(Rank);
      }
    }
  }

  @Override
  public void initialize() {
    enumerateHole();
    super.initialize();
  }

  @Override
  public boolean canHandleGroups(int[] groupsSizes) {
    return groupsSizes != null && groupsSizes.length == 2 && groupsSizes[0] == 2
        && groupsSizes[1] == 3;
  }

  @Override
  public boolean isCompatible(String gameId) {
    return "HE_POKER_FLOP".equals(gameId);
  }

}

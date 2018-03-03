package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

// import measure.RiverHS;

public class RiverIndexer extends Indexer {
  // Says to which rank pattern this rank belongs.
  private int[][][][][][][] rankPatternIndex = new int[7][7][7][7][7][7][7];

  private static final int[] n = {0, 12, 23, 33, 42, 50, 57, 63, 68, 72, 75, 77, 78};
  private static final int[] m = {0, 78, 144, 199, 244, 280, 308, 329, 344, 354, 360, 363, 364};
  private static final int[] o =
      {0, 364, 650, 870, 1035, 1155, 1239, 1295, 1330, 1350, 1360, 1364, 1365};
  private static final int[] p =
      {0, 1365, 2366, 3081, 3576, 3906, 4116, 4242, 4312, 4347, 4362, 4367, 4368};

  public RiverIndexer() {
    super(52402675);
    numCards = 7;
    rankPatternSuits = new RiverSuit[214];
    numRankPattern = new int[214];
    rankPositionMap = new int[563199];
    rankIndexMap = new int[563199];

    int sizev[] = new int[] {7, 7, 7, 7, 7, 7, 7};

    // Set all rankPatternIndex entries to -1
    Helper.init_int7(rankPatternIndex, sizev, -1);
  }

  public float handEval(int cards[]) {
    // return RiverHS.riverklaatuHS(cards);
    return -1;
  }

  /*
   * Rank index of the board [0, 6188-1]
   */
  private static int boardRankIndex(int bRank[]) {
    return p[bRank[0]] + o[bRank[1]] + m[bRank[2]] + n[bRank[3]] + bRank[4];
  }

  /*
   * Creates a unique index for every rank (hole rank, board rank) combination.
   */
  @Override
  public int handRankIndex(int Rank[]) {
    int hRank[] = new int[] {Rank[0], Rank[1]};
    int bRank[] = new int[] {Rank[2], Rank[3], Rank[4], Rank[5], Rank[6]};

    int hridx = holeRankIndex(hRank);
    int bridx = boardRankIndex(bRank);

    return bridx * 91 + hridx;
  }

  /*
   * Only used when doing a dry run to count and generate tables.
   */
  private void countRankSuits(int[] Rank) {
    int r[] = lowestRank(Rank);
    int rankIsoIndex = rankPatternIndex[r[0]][r[1]][r[2]][r[3]][r[4]][r[5]][r[6]];

    // Haven't come upon this rank pattern yet, add it.
    if (rankIsoIndex == -1) {
      rankPatternSuits[rankPatternCount] = new RiverSuit();
      Suits s = rankPatternSuits[rankPatternCount];
      rankPatternIndex[r[0]][r[1]][r[2]][r[3]][r[4]][r[5]][r[6]] = rankPatternCount;

      s.enumSuits(r);

      if (generationDebug) {
        Helper.printArray("", r);
        System.out.println("Num suits:" + s.getSize() + " rankindex:" + rankPatternCount);
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
    for (int i = 0; i < 13; i++) {
      for (int j = i; j < 13; j++) {
        for (int k = j; k < 13; k++) {
          for (int l = k; l < 13; l++) {
            for (int m = l; m < 13; m++) {
              Rank[2] = i;
              Rank[3] = j;
              Rank[4] = k;
              Rank[5] = l;
              Rank[6] = m;

              // skip 5 of a kind
              if (Helper.numMaxRanks(Rank) > 4) {
                continue;
              }
              countRankSuits(Rank);
            }
          }
        }
      }
    }
  }

  private void enumerateHole() {
    int[] Rank = new int[7];

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
        && groupsSizes[1] == 5;
  }

  @Override
  public boolean isCompatible(String gameId) {
    return "HE_POKER_RIVER".equals(gameId);
  }

}

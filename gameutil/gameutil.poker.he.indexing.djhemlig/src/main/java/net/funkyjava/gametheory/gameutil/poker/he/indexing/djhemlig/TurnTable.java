package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

// import measure.TurnEHS;

public class TurnTable extends Table {
  // Says to which rank pattern this rank belongs.
  private int[][][][][][] rankPatternIndex = new int[6][6][6][6][6][6];

  // For boardRankIndex
  private static final int[] n = {0, 12, 23, 33, 42, 50, 57, 63, 68, 72, 75, 77, 78};
  private static final int[] m = {0, 78, 144, 199, 244, 280, 308, 329, 344, 354, 360, 363, 364};
  private static final int[] o =
      {0, 364, 650, 870, 1035, 1155, 1239, 1295, 1330, 1350, 1360, 1364, 1365};

  private final int tableSize = 15111642;

  public TurnTable() {
    numCards = 6;
    LUT = new float[tableSize];
    rankPatternSuits = new TurnSuit[89];
    numRankPattern = new int[89];
    rankPositionMap = new int[165620];
    rankIndexMap = new int[165620];

    int sizev[] = new int[] {6, 6, 6, 6, 6, 6};

    // Set all rankPatternIndex entries to -1
    Helper.init_int6(rankPatternIndex, sizev, -1);
  }

  @Override
  public float handEval(int cards[]) {
    // return TurnEHS.turnklaatuEHS(cards);
    return -1;
  }

  /*
   * Rank index of the board [0, 1820-1]
   */
  private static int boardRankIndex(int bRank[]) {
    return o[bRank[0]] + m[bRank[1]] + n[bRank[2]] + bRank[3];
  }

  /*
   * Creates a unique index for every rank (hole rank, board rank) combination.
   */
  @Override
  public int handRankIndex(int Rank[]) {
    int hRank[] = new int[] {Rank[0], Rank[1]};
    int bRank[] = new int[] {Rank[2], Rank[3], Rank[4], Rank[5]};

    int hridx = holeRankIndex(hRank);
    int bridx = boardRankIndex(bRank);

    return bridx * 91 + hridx;
  }

  /*
   * Only used when doing a dry run to count and generate tables.
   */
  private void countRankSuits(int[] Rank) {
    int r[] = lowestRank(Rank);
    int rankIsoIndex = rankPatternIndex[r[0]][r[1]][r[2]][r[3]][r[4]][r[5]];

    // Haven't come upon this rank pattern yet, add it.
    if (rankIsoIndex == -1) {
      rankPatternSuits[rankPatternCount] = new TurnSuit();
      Suits t = rankPatternSuits[rankPatternCount];
      rankPatternIndex[r[0]][r[1]][r[2]][r[3]][r[4]][r[5]] = rankPatternCount;

      t.enumSuits(r);

      if (generationDebug) {
        Helper.printArray("", r);
        System.out.println("Num suits:" + t.getSize() + " rankindex:" + rankPatternCount);
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
            Rank[2] = i;
            Rank[3] = j;
            Rank[4] = k;
            Rank[5] = l;

            // skip 5 of a kind
            if (Helper.numMaxRanks(Rank) > 4) {
              continue;
            }

            if (dryrun == 1) {
              countRankSuits(Rank);
            } else {
              enumerateSuits(Rank);
            }
          }
        }
      }
    }
  }

  private void enumerateHole() {
    int[] Rank = new int[6];

    for (int i = 0; i < 13; i++) {
      for (int j = i; j < 13; j++) {
        Rank[0] = i;
        Rank[1] = j;

        enumerateBoard(Rank);
      }
    }
  }

  @Override
  public void initializeTable() {
    enumerateHole();

    if (!Helper.load("turnehs.dat", LUT)) {
      dryrun = 0;
      enumerateHole();

      // wait for the threads to finish their work (1 can be in progress)
      // waitForThreads();

      // now write to disk
      Helper.save("turnehs.dat", LUT);
    }
  }

}

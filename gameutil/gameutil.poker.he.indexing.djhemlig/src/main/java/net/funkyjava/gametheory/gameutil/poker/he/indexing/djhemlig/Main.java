package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

public class Main {
  public static void floptestSpeed(FlopTable f) {
    int board[] = new int[3];
    long start = System.currentTimeMillis();

    int nboards = 0;
    for (int i = 0; i < 52; i++) {
      for (int j = i + 1; j < 52; j++) {
        for (int k = j + 1; k < 52; k++) {
          board[0] = i;
          board[1] = j;
          board[2] = k;

          f.lookupAll(board);

          nboards++;
        }
      }
    }

    long end = System.currentTimeMillis();
    System.out.println(nboards + " boards lookup in:" + (end - start) + " ms");
  }

  public static void flopTest() {
    FlopTable f = new FlopTable();

    f.initialize();

    int tested = f.testLUT(100);
    System.out.println("tested:" + tested);

    floptestSpeed(f);
  }

  public static void turnTest() {
    TurnTable f = new TurnTable();

    f.initialize();

    int tested = f.testLUT(800);
    System.out.println("tested:" + tested);
  }

  public static void rivertestSpeed(RiverTable f) {
    int board[] = new int[5];
    long start = System.currentTimeMillis();

    int nboards = 0;
    for (int i = 0; i < 52; i++) {
      for (int j = i + 1; j < 52; j++) {
        for (int k = j + 1; k < 52; k++) {
          for (int l = k + 1; l < 52; l++) {
            for (int m = l + 1; m < 52; m++) {
              board[0] = i;
              board[1] = j;
              board[2] = k;
              board[3] = l;
              board[4] = m;

              f.lookupAll(board);

              nboards++;
            }
          }
        }
      }
    }

    long end = System.currentTimeMillis();
    System.out.println(nboards + " river boards lookup in:" + (end - start) + " ms");
  }

  public static void riverTest() {
    RiverTable f = new RiverTable();

    f.initialize();

    int tested = f.testLUT(3000);
    System.out.println("tested:" + tested);
    rivertestSpeed(f);
  }

  /*
   * For testing of how it works.
   */
  public static void main(String[] args) {
    flopTest();
    turnTest();
    riverTest();
  }
}

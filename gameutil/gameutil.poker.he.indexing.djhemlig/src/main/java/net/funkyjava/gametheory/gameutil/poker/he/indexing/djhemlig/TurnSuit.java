package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

public class TurnSuit extends Suits {
  private int suitMap[][][][][][] = new int[4][4][4][4][4][4];
  private int isoSuitIndex = 0;

  TurnSuit() {
    int sizev[] = new int[] {4, 4, 4, 4, 4, 4};

    Helper.init_int6(suitMap, sizev, -1);

    for (int i = 0; i < sameHand.length; i++) {
      sameHand[i] = -1;
    }
  }

  @Override
  public int getPatternIndex(int p[]) {
    return suitMap[p[0]][p[1]][p[2]][p[3]][p[4]][p[5]];
  }

  // Suits 0..3, Ranks 0..5, 6 cards, max card index 5*4+3
  // see http://en.wikipedia.org/wiki/Combinadic
  private static int sameHand[] = new int[2 * 10626];

  private static int sameHandIndex(int ranks[], int suits[]) {
    int[] cards = Helper.sortedIsoBoard(ranks, suits);
    int suited = (suits[0] == suits[1]) ? 1 : 0;

    int hidx = 0;
    for (int i = 0; i < 4; i++) {
      hidx += Helper.nchoosek(cards[i], i + 1);
    }

    hidx += suited * 10626;

    return hidx;
  }

  private static int sameBoard(int ranks[], int suits[]) {
    int hidx = sameHandIndex(ranks, suits);

    return sameHand[hidx];
  }

  private static void addSameBoard(int ranks[], int suits[], int index) {
    int hidx = sameHandIndex(ranks, suits);

    sameHand[hidx] = index;
  }

  private int getSuitMapIndex(int s[]) {
    return suitMap[s[0]][s[1]][s[2]][s[3]][s[4]][s[5]];
  }

  private void setSuitMapIndex(int s[], int index) {
    suitMap[s[0]][s[1]][s[2]][s[3]][s[4]][s[5]] = index;
  }

  private void addSuit(int[] Rank, int suits[]) {
    int isuit[] = lowestSuit(suits);

    if (!Helper.isoHandCheck(Rank, isuit)) {
      return;
    }

    int seenHandIndex = sameBoard(Rank, isuit);
    if (seenHandIndex > -1) {
      setSuitMapIndex(suits, seenHandIndex);
      return;
    }

    int lowSuitIndex = getSuitMapIndex(isuit);
    // we haven't come across this suit iso pattern yet
    if (lowSuitIndex == -1) {
      setSuitMapIndex(isuit, isoSuitIndex);
      setSuitMapIndex(suits, isoSuitIndex);

      addSameBoard(Rank, isuit, isoSuitIndex);

      patterns.add(isuit);
      isoSuitIndex++;
    } else {
      setSuitMapIndex(suits, lowSuitIndex);
    }
  }

  /*
   * Enumerate the all suits
   */
  @Override
  public void enumSuits(int[] Rank) {
    int suits[] = new int[6];

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        for (int k = 0; k < 4; k++) {
          for (int l = 0; l < 4; l++) {
            for (int m = 0; m < 4; m++) {
              for (int n = 0; n < 4; n++) {
                suits[0] = i;
                suits[1] = j;
                suits[2] = k;
                suits[3] = l;
                suits[4] = m;
                suits[5] = n;

                addSuit(Rank, suits);
              }
            }
          }
        }
      }
    }
  }
}

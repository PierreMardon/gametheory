package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

import java.util.ArrayList;

abstract public class Suits {
  /*
   * The list of suit patterns we see are the only suit patterns mattering with respect to this rank
   * pattern. Only used during enumeration, not lookup.
   */
  public ArrayList<int[]> patterns = new ArrayList<>();

  abstract public int getPatternIndex(int p[]);

  abstract public void enumSuits(int Rank[]);

  public int getSize() {
    return patterns.size();
  }

  public int[] getPattern(int i) {
    return patterns.get(i);
  }

  /*
   * Returns the isomorphic lowest suit pattern.
   */
  public static int[] lowestSuit(int suits[]) {
    int map[] = new int[] {-1, -1, -1, -1};
    int isoSuit[] = new int[suits.length];

    int currentsuit = 0;

    for (int i = 0; i < suits.length; i++) {
      if (map[suits[i]] == -1) {
        map[suits[i]] = currentsuit;
        currentsuit++;
      }
      isoSuit[i] = map[suits[i]];
    }
    return isoSuit;
  }

  /*
   * Returns vector with number of suits of each type.
   */
  public static int[] suitCount(int suits[]) {
    int count[] = new int[4];

    for (int i = 0; i < suits.length; i++) {
      count[suits[i]]++;
    }

    return count;
  }
}

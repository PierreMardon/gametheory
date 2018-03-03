package net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Random;

public class Helper {
  // generated with matlab
  public static int[][] nchoosektable = new int[][] {{1, 0, 0, 0, 0, 0, 0, 0},
      {1, 1, 0, 0, 0, 0, 0, 0}, {1, 2, 1, 0, 0, 0, 0, 0}, {1, 3, 3, 1, 0, 0, 0, 0},
      {1, 4, 6, 4, 1, 0, 0, 0}, {1, 5, 10, 10, 5, 1, 0, 0}, {1, 6, 15, 20, 15, 6, 1, 0},
      {1, 7, 21, 35, 35, 21, 7, 1}, {1, 8, 28, 56, 70, 56, 28, 8}, {1, 9, 36, 84, 126, 126, 84, 36},
      {1, 10, 45, 120, 210, 252, 210, 120}, {1, 11, 55, 165, 330, 462, 462, 330},
      {1, 12, 66, 220, 495, 792, 924, 792}, {1, 13, 78, 286, 715, 1287, 1716, 1716},
      {1, 14, 91, 364, 1001, 2002, 3003, 3432}, {1, 15, 105, 455, 1365, 3003, 5005, 6435},
      {1, 16, 120, 560, 1820, 4368, 8008, 11440}, {1, 17, 136, 680, 2380, 6188, 12376, 19448},
      {1, 18, 153, 816, 3060, 8568, 18564, 31824}, {1, 19, 171, 969, 3876, 11628, 27132, 50388},
      {1, 20, 190, 1140, 4845, 15504, 38760, 77520}, {1, 21, 210, 1330, 5985, 20349, 54264, 116280},
      {1, 22, 231, 1540, 7315, 26334, 74613, 170544},
      {1, 23, 253, 1771, 8855, 33649, 100947, 245157},
      {1, 24, 276, 2024, 10626, 42504, 134596, 346104},
      {1, 25, 300, 2300, 12650, 53130, 177100, 480700},
      {1, 26, 325, 2600, 14950, 65780, 230230, 657800},
      {1, 27, 351, 2925, 17550, 80730, 296010, 888030},
      {1, 28, 378, 3276, 20475, 98280, 376740, 1184040},
      {1, 29, 406, 3654, 23751, 118755, 475020, 1560780},
      {1, 30, 435, 4060, 27405, 142506, 593775, 2035800},
      {1, 31, 465, 4495, 31465, 169911, 736281, 2629575},
      {1, 32, 496, 4960, 35960, 201376, 906192, 3365856},
      {1, 33, 528, 5456, 40920, 237336, 1107568, 4272048},
      {1, 34, 561, 5984, 46376, 278256, 1344904, 5379616},
      {1, 35, 595, 6545, 52360, 324632, 1623160, 6724520},
      {1, 36, 630, 7140, 58905, 376992, 1947792, 8347680},
      {1, 37, 666, 7770, 66045, 435897, 2324784, 10295472},
      {1, 38, 703, 8436, 73815, 501942, 2760681, 12620256},
      {1, 39, 741, 9139, 82251, 575757, 3262623, 15380937},
      {1, 40, 780, 9880, 91390, 658008, 3838380, 18643560},
      {1, 41, 820, 10660, 101270, 749398, 4496388, 22481940},
      {1, 42, 861, 11480, 111930, 850668, 5245786, 26978328},
      {1, 43, 903, 12341, 123410, 962598, 6096454, 32224114},
      {1, 44, 946, 13244, 135751, 1086008, 7059052, 38320568},
      {1, 45, 990, 14190, 148995, 1221759, 8145060, 45379620},
      {1, 46, 1035, 15180, 163185, 1370754, 9366819, 53524680},
      {1, 47, 1081, 16215, 178365, 1533939, 10737573, 62891499},
      {1, 48, 1128, 17296, 194580, 1712304, 12271512, 73629072},
      {1, 49, 1176, 18424, 211876, 1906884, 13983816, 85900584},
      {1, 50, 1225, 19600, 230300, 2118760, 15890700, 99884400},
      {1, 51, 1275, 20825, 249900, 2349060, 18009460, 115775100},
      {1, 52, 1326, 22100, 270725, 2598960, 20358520, 133784560},};

  // Choose k out of n items with no replacement. if k > n defined as 0 here.
  public static int nchoosek(int n, int k) {
    return nchoosektable[n][k];
  }

  public static void init_int7(int v[][][][][][][], int size[], int value) {
    for (int i = 0; i < size[0]; i++) {
      for (int j = 0; j < size[1]; j++) {
        for (int k = 0; k < size[2]; k++) {
          for (int l = 0; l < size[3]; l++) {
            for (int m = 0; m < size[4]; m++) {
              for (int n = 0; n < size[5]; n++) {
                for (int o = 0; o < size[6]; o++) {
                  v[i][j][k][l][m][n][o] = value;
                }
              }
            }
          }
        }
      }
    }
  }

  public static void init_int6(int v[][][][][][], int size[], int value) {
    for (int i = 0; i < size[0]; i++) {
      for (int j = 0; j < size[1]; j++) {
        for (int k = 0; k < size[2]; k++) {
          for (int l = 0; l < size[3]; l++) {
            for (int m = 0; m < size[4]; m++) {
              for (int n = 0; n < size[5]; n++) {
                v[i][j][k][l][m][n] = value;
              }
            }
          }
        }
      }
    }
  }

  public static void init_int5(int v[][][][][], int size[], int value) {
    for (int i = 0; i < size[0]; i++) {
      for (int j = 0; j < size[1]; j++) {
        for (int k = 0; k < size[2]; k++) {
          for (int l = 0; l < size[3]; l++) {
            for (int m = 0; m < size[4]; m++) {
              v[i][j][k][l][m] = value;
            }
          }
        }
      }
    }
  }

  public static void init_int3(int v[][][], int size[], int value) {
    for (int i = 0; i < size[0]; i++) {
      for (int j = 0; j < size[1]; j++) {
        for (int k = 0; k < size[2]; k++) {
          v[i][j][k] = value;
        }
      }
    }
  }

  /*
   * Get a random ncards hand, each card 0..51
   */
  public static int[] randomHand(int ncards) {
    Random r = new Random();
    int hand[] = new int[ncards];
    int index = 0;

    while (index < ncards) {
      boolean foundcard = false;
      int c = r.nextInt(52);

      for (int j = 0; j < index; j++) {
        if (hand[j] == c) {
          foundcard = true;
        }
      }

      if (foundcard) {
        continue;
      }
      hand[index] = c;
      index++;
    }

    return hand;
  }

  /*
   * Gives a map saying which element should be where for h to be sorted. Note that it changes
   * contents of h.
   */
  public static int[] sortMap(int h[]) {
    int n = h.length;
    int map[] = new int[n];

    for (int i = 0; i < n; i++) {
      int least = 0;

      for (int j = 0; j < n; j++) {
        if (h[j] < h[least]) {
          least = j;
        }
      }

      map[i] = least;
      h[least] = 100;
    }

    return map;
  }

  public static void printArray(String s, int[] arg) {
    int n = arg.length;
    System.out.print(s + "{" + arg[0]);
    for (int i = 1; i < n; i++) {
      System.out.print("," + arg[i]);
    }

    System.out.println("}");
  }

  public static void printArrayFloat(String s, float[] arg) {
    int n = arg.length;
    System.out.print(s + "{" + arg[0]);
    for (int i = 1; i < n; i++) {
      System.out.print("," + arg[i]);
    }

    System.out.println("}");
  }

  public static int[] sortedIsoHand(int ranks[], int suits[]) {
    int[] cards = new int[ranks.length];

    for (int i = 0; i < ranks.length; i++) {
      cards[i] = ranks[i] * 4 + suits[i];
    }

    Arrays.sort(cards);

    return cards;
  }

  public static int[] sortedIsoBoard(int ranks[], int suits[]) {
    int[] board = new int[ranks.length - 2];

    for (int i = 0; i < ranks.length - 2; i++) {
      board[i] = ranks[i + 2] * 4 + suits[i + 2];
    }

    Arrays.sort(board);

    return board;
  }

  /*
   * Returns the number of ranks of the most common rank.
   */
  public static int numMaxRanks(int ranks[]) {
    int count[] = new int[13];
    int max = 0;

    for (int i = 0; i < ranks.length; i++) {
      count[ranks[i]]++;

      if (count[ranks[i]] > count[max]) {
        max = ranks[i];
      }

    }
    return count[max];
  }

  /*
   * In the iso model ranks 0..ncards-1, suits 0..3 Check if there are any duplicate cards.
   */
  public static boolean isoHandCheck(int ranks[], int suits[]) {
    int[] cards = sortedIsoHand(ranks, suits);
    int ncards = cards.length;

    for (int i = 1; i < ncards; i++) {
      if (cards[i - 1] == cards[i]) {
        return false;
      }
    }

    return true;
  }

  public static int[] getHand(int[] Rank, int[] suit) {
    int ncards = Rank.length;
    int all[] = new int[ncards];

    for (int i = 0; i < ncards; i++) {
      all[i] = Rank[i] * 4 + suit[i];
    }

    return all;
  }

  public static void save(String path, float table[]) {
    try (FileOutputStream fos = new FileOutputStream(path); FileChannel fc = fos.getChannel()) {
      ByteBuffer buff = ByteBuffer.allocate(table.length * 4);
      buff.asFloatBuffer().put(table);
      fc.write(buff);
      fc.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static boolean load(String path, float table[]) {
    try (FileInputStream fis = new FileInputStream(path)) {
      FileChannel fc = fis.getChannel();
      ByteBuffer buff = ByteBuffer.allocate(table.length * 4);
      fc.read(buff);
      buff.flip();
      for (int i = 0; i < table.length; i++) {
        table[i] = buff.getFloat();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }
}

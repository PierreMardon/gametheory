package net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo;

public class Evaluator {

  /*
   * Card to integer conversions:
   *
   * 2c = 1 2d = 14 2h = 27 2s = 40 3c = 2 3d = 15 3h = 28 3s = 41 4c = 3 4d = 16 4h = 29 4s = 42 5c
   * = 4 5d = 17 5h = 30 5s = 43 6c = 5 6d = 18 6h = 31 6s = 44 7c = 6 7d = 19 7h = 32 7s = 45 8c =
   * 7 8d = 20 8h = 33 8s = 46 9c = 8 9d = 21 9h = 34 9s = 47 Tc = 9 Td = 22 Th = 35 Ts = 48 Jc = 10
   * Jd = 23 Jh = 36 Js = 49 Qc = 11 Qd = 24 Qh = 37 Qs = 50 Kc = 12 Kd = 25 Kh = 38 Ks = 51 Ac = 13
   * Ad = 26 Ah = 39 As = 52
   */

  public final static int NO_SUIT = 0;
  public final static int CLUBS = 1;
  public final static int DIAMONDS = 2;
  public final static int HEARTS = 3;
  public final static int SPADES = 4;

  public final static int BAD_CARD = -1;
  public final static int DEUCE = 2;
  public final static int THREE = 3;
  public final static int FOUR = 4;
  public final static int FIVE = 5;
  public final static int SIX = 6;
  public final static int SEVEN = 7;
  public final static int EIGHT = 8;
  public final static int NINE = 9;
  public final static int TEN = 10;
  public final static int JACK = 11;
  public final static int QUEEN = 12;
  public final static int KING = 13;
  public final static int ACE = 14;

  public final static int NUM_SUITS = 4;
  public final static int NUM_RANKS = 13;
  public final static int NUM_CARDS = 52;

  public static int[] handRanks = new int[32487834]; // array to hold hand
                                                     // rank lookup table
  public static boolean verbose = true; // toggles verbose mode

  private static int[] hand; // re-usable array to hold cards in a hand
  private static long[] keys = new long[612978]; // array to hold key lookup
                                                 // table
  private static int numKeys = 1; // counter for number of defined keys in key
                                  // array
  private static long maxKey = 0; // holds current maximum key value
  private static int numCards = 0; // re-usable counter for number of cards in
                                   // a hand
  private static int cardIndex = 0; // re-usable index for cards in a hands
  private static int maxHandRankIndex = 0;

  private static long startTimer;
  private static long stopTimer;

  // Inserts a key into the key array and returns the insertion index.
  public static int insertKey(long key) {

    // check to see if key is valid
    if (key == 0) {
      return 0;
    }

    // short circuit insertion for most common cases
    if (key >= maxKey) {
      if (key > maxKey) {
        keys[numKeys++] = key; // appends the new key to the key array
        maxKey = key;
      }
      return numKeys - 1;
    }

    // use binary search to find insertion point for new key
    int low = -1;
    int high = numKeys;
    int pivot;
    long difference;

    while (high - low > 1) {
      pivot = (low + high) >>> 1;
      difference = keys[pivot] - key;
      if (difference > 0) {
        high = pivot;
      } else if (difference < 0) {
        low = pivot;
      } else {
        return pivot; // key already exists
      }
    }

    // key does not exist so must be inserted
    System.arraycopy(keys, high, keys, high + 1, numKeys - high);
    keys[high] = key;

    numKeys++;
    return high;

  } // END insertKey method

  // Returns a key for the hand created by adding a new card to the hand
  // represented by the given key. Returns 0 if new card already appears in
  // hand.
  private static long makeKey(long baseKey, int newCard) {

    int[] suitCount = new int[NUM_SUITS + 1]; // number of times a suit
                                              // appears in a hand
    int[] rankCount = new int[NUM_RANKS + 1]; // number of times a rank
                                              // appears in a hand
    hand = new int[8];

    // extract the hand represented by the key value
    for (cardIndex = 0; cardIndex < 6; cardIndex++) {

      // hand[0] is used to hold the new card
      hand[cardIndex + 1] = (int) ((baseKey >>> (8 * cardIndex)) & 0xFF);
    }

    hand[0] = formatCard8bit(newCard);

    // examine the hand to determine number of cards and rank/suit counts
    for (numCards = 0; hand[numCards] != 0; numCards++) {
      suitCount[hand[numCards] & 0xF]++;
      rankCount[(hand[numCards] >>> 4) & 0xF]++;

      // check to see if new card is already contained in hand (rank and
      // suit considered)
      if (numCards != 0 && hand[0] == hand[numCards]) {
        return 0;
      }
    }

    // check to see if we already have four of a particular rank
    if (numCards > 4) {
      for (int rank = 1; rank < 14; rank++) {
        if (rankCount[rank] > 4) {
          return 0;
        }
      }
    }

    // determine the minimum number of suits required for a flush to be
    // possible
    int minSuitCount = numCards - 2;

    // check to see if suit is significant
    if (minSuitCount > 1) {
      // examine each card in the hand
      for (cardIndex = 0; cardIndex < numCards; cardIndex++) {
        // if the suit is not significant then strip it from the card
        if (suitCount[hand[cardIndex] & 0xF] < minSuitCount) {
          hand[cardIndex] &= 0xF0;
        }
      }
    }

    sortHand();

    long key = 0;
    for (int i = 0; i < 7; i++) {
      key += (long) hand[i] << (i * 8);
    }

    return key;

  } // END makeKey method

  // Formats and returns a card in 8-bit packed representation.
  private static int formatCard8bit(int card) {

    // 8-Bit Packed Card Representation
    // +--------+
    // |rrrr--ss|
    // +--------+
    // r = rank of card (deuce = 1, trey = 2, four = 3, five = 4,..., ace =
    // 13)
    // s = suit of card (suits are arbitrary, can take value from 0 to 3)

    card--;
    return (((card >>> 2) + 1) << 4) + (card & 3) + 1;

  } // END formatCard8bit method

  // Sorts the hand using Bose-Nelson Sorting Algorithm (N = 7).
  private static void sortHand() {
    swapCard(0, 4);
    swapCard(1, 5);
    swapCard(2, 6);
    swapCard(0, 2);
    swapCard(1, 3);
    swapCard(4, 6);
    swapCard(2, 4);
    swapCard(3, 5);
    swapCard(0, 1);
    swapCard(2, 3);
    swapCard(4, 5);
    swapCard(1, 4);
    swapCard(3, 6);
    swapCard(1, 2);
    swapCard(3, 4);
    swapCard(5, 6);
  } // End sortHand method

  // Swaps card i with card j.
  private static void swapCard(int i, int j) {
    if (hand[i] < hand[j]) {
      hand[i] ^= hand[j];
      hand[j] ^= hand[i];
      hand[i] ^= hand[j];
    }
  } // END swapCard method

  // Determines the relative strength of a hand (the hand is given by its
  // unique key value).
  private static int getHandRank(long key) {

    // The following method implements a modified version of "Cactus Kev's
    // Five-Card
    // Poker Hand Evaluator" to determine the relative strength of two
    // five-card hands.
    // Reference: http://www.suffecool.net/poker/evaluator.html

    hand = new int[8];
    int currentCard;
    int rank;
    int handRank = 9999;
    int holdrank = 9999;
    int suit = 0;
    int numCards = 0;

    final int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};

    if (key != 0) {

      for (cardIndex = 0; cardIndex < 7; cardIndex++) {

        currentCard = (int) ((key >>> (8 * cardIndex)) & 0xFF);
        if (currentCard == 0) {
          break;
        }
        numCards++;

        // Cactus Kev Card Representation
        // +--------+--------+--------+--------+
        // |xxxbbbbb|bbbbbbbb|cdhsrrrr|xxpppppp|
        // +--------+--------+--------+--------+
        // p = prime number of rank (deuce = 2, trey = 3, four = 5, five
        // = 7,..., ace = 41)
        // r = rank of card (deuce = 0, trey = 1, four = 2, five =
        // 3,..., ace = 12)
        // cdhs = suit of card
        // b = bit turned on depending on rank of card

        // extract suit and rank from 8-bit packed representation
        rank = (currentCard >>> 4) - 1;
        suit = currentCard & 0xF;

        // change card representation to Cactus Kev Representation
        hand[cardIndex] = primes[rank] | (rank << 8) | (1 << (suit + 11)) | (1 << (16 + rank));
      }

      switch (numCards) {
        case 5:

          holdrank = eval_5hand(hand[0], hand[1], hand[2], hand[3], hand[4]);
          break;

        case 6:

          // Cactus Kev's Evaluator ranks hands from 1 (Royal Flush) to
          // 7462 (Seven High Card)
          holdrank = eval_5hand(hand[0], hand[1], hand[2], hand[3], hand[4]);
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[2], hand[3], hand[5]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[2], hand[4], hand[5]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[3], hand[4], hand[5]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[2], hand[3], hand[4], hand[5]));
          holdrank = Math.min(holdrank, eval_5hand(hand[1], hand[2], hand[3], hand[4], hand[5]));
          break;

        case 7:

          holdrank = eval_5hand(hand[0], hand[1], hand[2], hand[3], hand[4]);
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[2], hand[3], hand[5]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[2], hand[3], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[2], hand[4], hand[5]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[2], hand[4], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[2], hand[5], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[3], hand[4], hand[5]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[3], hand[4], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[3], hand[5], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[1], hand[4], hand[5], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[2], hand[3], hand[4], hand[5]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[2], hand[3], hand[4], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[2], hand[3], hand[5], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[2], hand[4], hand[5], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[0], hand[3], hand[4], hand[5], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[1], hand[2], hand[3], hand[4], hand[5]));
          holdrank = Math.min(holdrank, eval_5hand(hand[1], hand[2], hand[3], hand[4], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[1], hand[2], hand[3], hand[5], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[1], hand[2], hand[4], hand[5], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[1], hand[3], hand[4], hand[5], hand[6]));
          holdrank = Math.min(holdrank, eval_5hand(hand[2], hand[3], hand[4], hand[5], hand[6]));
          break;

        default:

          System.out.println("ERROR: Invalid hand in GetRank method.");
          break;

      }

      // Hand Rank Representation
      // +--------+--------+
      // |hhhheeee|eeeeeeee|
      // +--------+--------+
      // h = poker hand (1 = High Card, 2 = One Pair, 3 = Two Pair,..., 9
      // = Straight Flush)
      // e = equivalency class (Rank of equivalency class relative to base
      // hand)

      // +-----------------------------------+----------------------------------+-----------------+
      // 5-Card Equivalency Classes 7-Card Equivalency Classes
      // +-----------------------------------+----------------------------------+-----------------+
      // 1277 407 High Card
      // 2860 1470 One Pair
      // 858 763 Two Pair
      // 858 575 Three of a Kind
      // 10 10 Straight
      // 1277 1277 Flush
      // 156 156 Full House
      // 156 156 Four of a Kind
      // 10 10 Straight Flush
      // +----------+------------------------+----------------------------------+-----------------+
      // Total: 7462 4824
      // +----------+------------------------+----------------------------------+-----------------+

      handRank = 7463 - holdrank; // Invert ranking metric (1 is now worst
                                  // hand)

      if (handRank < 1278) {
        handRank = handRank - 0 + 4096 * 1; // High Card
      } else if (handRank < 4138) {
        handRank = handRank - 1277 + 4096 * 2; // One Pair
      } else if (handRank < 4996) {
        handRank = handRank - 4137 + 4096 * 3; // Two Pair
      } else if (handRank < 5854) {
        handRank = handRank - 4995 + 4096 * 4; // Three of a Kind
      } else if (handRank < 5864) {
        handRank = handRank - 5853 + 4096 * 5; // Straight
      } else if (handRank < 7141) {
        handRank = handRank - 5863 + 4096 * 6; // Flush
      } else if (handRank < 7297) {
        handRank = handRank - 7140 + 4096 * 7; // Full House
      } else if (handRank < 7453) {
        handRank = handRank - 7296 + 4096 * 8; // Four of a Kind
      } else {
        handRank = handRank - 7452 + 4096 * 9; // Straight Flush
      }

    }
    return handRank;

  } // END getHandRank method

  private static int getIndex(int key) {

    // use binary search to find key
    int low = -1;
    int high = 4888;
    int pivot;

    while (high - low > 1) {
      pivot = (low + high) >>> 1;
      if (Products.table[pivot] > key) {
        high = pivot;
      } else if (Products.table[pivot] < key) {
        low = pivot;
      } else {
        return pivot;
      }
    }
    return -1;

  } // END getIndex method

  private static int eval_5hand(int c1, int c2, int c3, int c4, int c5) {
    int q = (c1 | c2 | c3 | c4 | c5) >> 16;
    short s;

    // check for Flushes and Straight Flushes
    if ((c1 & c2 & c3 & c4 & c5 & 0xF000) != 0) {
      return Flushes.table[q];
    }

    // check for Straights and High Card hands
    if ((s = Unique.table[q]) != 0) {
      return s;
    }

    q = (c1 & 0xFF) * (c2 & 0xFF) * (c3 & 0xFF) * (c4 & 0xFF) * (c5 & 0xFF);
    q = getIndex(q);

    return Values.table[q];

  } // END eval_5hand method

  private static boolean generatedTables = false;

  public static void generateTables() {
    if (generatedTables) {
      return;
    }
    generatedTables = true;
    int card;
    int handRank;
    int keyIndex;
    long key;

    if (verbose) {
      System.out.print("\nGenerating and sorting keys...");
      startTimer = System.currentTimeMillis();
    }

    for (keyIndex = 0; keys[keyIndex] != 0 || keyIndex == 0; keyIndex++) {

      for (card = 1; card < 53; card++) { // add a card to each
                                          // previously
                                          // calculated key
        key = makeKey(keys[keyIndex], card); // create the new key

        if (numCards < 7) {
          insertKey(key); // insert the new key into the key
                          // lookup
                          // table
        }
      }
    }

    if (verbose) {
      stopTimer = System.currentTimeMillis();
      System.out.printf("done.\n\n%35s %d\n", "Number of Keys Generated:", (keyIndex + 1));
      System.out.printf("%35s %f seconds\n\n", "Time Required:",
          ((stopTimer - startTimer) / 1000.0));
      System.out.print("Generating hand ranks...");
      startTimer = System.currentTimeMillis();
    }

    for (keyIndex = 0; keys[keyIndex] != 0 || keyIndex == 0; keyIndex++) {

      for (card = 1; card < 53; card++) {
        key = makeKey(keys[keyIndex], card);

        if (numCards < 7) {
          handRank = insertKey(key) * 53 + 53; // if number of
                                               // cards
                                               // is < 7 insert
                                               // key
        } else {
          handRank = getHandRank(key); // if number of cards is 7
                                       // insert hand rank
        }

        maxHandRankIndex = keyIndex * 53 + card + 53; // calculate
                                                      // hand
                                                      // rank
                                                      // insertion
                                                      // index
        handRanks[maxHandRankIndex] = handRank; // populate hand
                                                // rank
                                                // lookup table with
                                                // appropriate value
      }

      if (numCards == 6 || numCards == 7) {
        // insert the hand rank into the hand rank lookup table
        handRanks[keyIndex * 53 + 53] = getHandRank(keys[keyIndex]);
      }
    }

    if (verbose) {
      stopTimer = System.currentTimeMillis();
      System.out.printf("done.\n\n%35s %f seconds\n\n", "Time Required:",
          ((stopTimer - startTimer) / 1000.0));
    }
  } // END generateTables method

  public static void main(String[] args) {
    generateTables();

    int c0, c1, c2, c3, c4, c5, c6;
    int u0, u1, u2, u3, u4, u5;
    int numHands = 0;
    int handRank;
    int[] handEnumerations = new int[10];
    int[][] equivalencyEnumerations = new int[10][3000];
    String[] handDescriptions = {"Invalid Hand", "High Card", "One Pair", "Two Pair",
        "Three of a Kind", "Straight", "Flush", "Full House", "Four of a Kind", "Straight Flush"};

    if (verbose) {
      System.out.print("Enumerating hand frequencies and equivalency classes...");
      startTimer = System.currentTimeMillis();
    }

    for (c0 = 1; c0 < 53; c0++) {
      u0 = handRanks[53 + c0];
      for (c1 = c0 + 1; c1 < 53; c1++) {
        u1 = handRanks[u0 + c1];
        for (c2 = c1 + 1; c2 < 53; c2++) {
          u2 = handRanks[u1 + c2];
          for (c3 = c2 + 1; c3 < 53; c3++) {
            u3 = handRanks[u2 + c3];
            for (c4 = c3 + 1; c4 < 53; c4++) {
              u4 = handRanks[u3 + c4];
              for (c5 = c4 + 1; c5 < 53; c5++) {
                u5 = handRanks[u4 + c5];
                for (c6 = c5 + 1; c6 < 53; c6++) {
                  handRank = handRanks[u5 + c6];
                  handEnumerations[handRank >>> 12]++;
                  equivalencyEnumerations[handRank >>> 12][handRank & 0xFFF]++;
                  numHands++;
                }
              }
            }
          }
        }
      }
    }

    if (verbose) {
      stopTimer = System.currentTimeMillis();
      System.out.printf("done.\n\n%35s %f seconds\n\n", "Time Required:",
          ((stopTimer - startTimer) / 1000.0));
    }

    System.out.println("SEVEN-CARD POKER HAND FREQUENCIES AND EQUIVALENCY CLASSES\n");
    System.out.printf(" %-17s %15s %15s\n", "HAND", "FREQUENCY", "CLASSES");
    System.out.println(" -------------------------------------------------");

    int sumEquivalency;
    int numClasses = 0;
    for (int i = handEnumerations.length - 1; i >= 0; i--) {
      sumEquivalency = 0;
      for (int j = 0; j < equivalencyEnumerations[i].length; j++) {
        if (equivalencyEnumerations[i][j] != 0) {
          sumEquivalency++;
        }
      }
      numClasses += sumEquivalency;
      System.out.printf(" %-17s %15d %15d\n", handDescriptions[i], handEnumerations[i],
          sumEquivalency);
    }
    System.out.println(" -------------------------------------------------");
    System.out.printf(" %-17s %15d %15d\n", "TOTAL", numHands, numClasses);
  }

} // END class Evaluator


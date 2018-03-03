package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.cards.CardsGroupsDrawingTask;
import net.funkyjava.gametheory.gameutil.cards.Deck52Cards;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;
import net.funkyjava.gametheory.io.Fillable;
import net.funkyjava.gametheory.io.IOUtils;

@Slf4j
public class ThreePlayersPreflopReducedEquityTable implements Fillable {

  private static final int[] onePlayerGroupsSize = {2};
  private static final int[] threePlayersGroupsSize = {2, 2, 2};

  private final WaughIndexer threePlayersIndexer = new WaughIndexer(threePlayersGroupsSize);
  @Getter
  private final WaughIndexer holeCardsIndexer = new WaughIndexer(onePlayerGroupsSize);
  @Getter
  private final int nbHoleCards = holeCardsIndexer.getIndexSize();

  @Getter
  private boolean computed = false;
  @Getter
  private boolean expanded = false;

  @Getter
  private final double[][][][][] reducedEquities =
      new double[nbHoleCards][nbHoleCards][nbHoleCards][][];

  public ThreePlayersPreflopReducedEquityTable() {

  }

  @AllArgsConstructor
  private static final class Permutator {

    private final int sourceIndex1, sourceIndex2, sourceIndex3;

    private final void inverseOrderEquities(final double[] source, final double[] dest) {
      dest[sourceIndex1] = source[0];
      dest[sourceIndex2] = source[1];
      dest[sourceIndex3] = source[2];
    }

    public final void permuteInverse(final double[][] source, final double[][] destination) {
      inverseOrderEquities(source[0], destination[0]);
      inverseOrderEquities(source[1], destination[1]);
      inverseOrderEquities(source[2], destination[2]);
      inverseOrderEquities(source[3], destination[3]);
      final double[][] tmp =
          new double[][] {destination[0], destination[1], destination[2], destination[3]};
      for (int i = 1; i < 4; i++) {
        final double[] arr = tmp[i];
        if (arr[0] == 0) {
          destination[ThreePlayersEquitiesIndexes.vilain1Vilain2] = arr;
        } else if (arr[1] == 0) {
          destination[ThreePlayersEquitiesIndexes.heroVilain2] = arr;
        } else if (arr[2] == 0) {
          destination[ThreePlayersEquitiesIndexes.heroVilain1] = arr;
        } else {
          destination[ThreePlayersEquitiesIndexes.heroVilain1Vilain2] = arr;
        }
      }
    }
  }

  private static final Permutator permutator123 = new Permutator(0, 1, 2);
  private static final Permutator permutator132 = new Permutator(0, 2, 1);
  private static final Permutator permutator231 = new Permutator(1, 2, 0);
  private static final Permutator permutator213 = new Permutator(1, 0, 2);
  private static final Permutator permutator312 = new Permutator(2, 0, 1);
  private static final Permutator permutator321 = new Permutator(2, 1, 0);

  private static final Permutator getPermutator(final int h1, final int h2, final int h3) {
    if (h1 <= h2 && h2 <= h3) {
      return permutator123;
    } else if (h1 <= h3 && h3 <= h2) {
      return permutator132;
    } else if (h2 <= h3 && h3 <= h1) {
      return permutator231;
    } else if (h2 <= h1 && h1 <= h3) {
      return permutator213;
    } else if (h3 <= h1 && h1 <= h2) {
      return permutator312;
    } else if (h3 <= h2 && h2 <= h1) {
      return permutator321;
    }

    throw new IllegalArgumentException();
  }

  public final void compute(final ThreePlayersPreflopEquityTable tables) {
    checkArgument(!computed, "Already computed");
    final double[][][] equities = tables.getEquities();
    final int nbHoleCards = this.nbHoleCards;
    final double[][][][][] reducedEquities = this.reducedEquities;
    final IntCardsSpec indexSpecs = holeCardsIndexer.getCardsSpec();
    final Deck52Cards deck = new Deck52Cards(indexSpecs);
    final WaughIndexer onePlayerIndexer = new WaughIndexer(onePlayerGroupsSize);
    deck.drawAllGroupsCombinations(threePlayersGroupsSize, (CardsGroupsDrawingTask) cardsGroups -> {
      final int heroIndex = onePlayerIndexer.indexOf(new int[][] {cardsGroups[0]});
      final int vilain1Index = onePlayerIndexer.indexOf(new int[][] {cardsGroups[1]});
      if (vilain1Index < heroIndex) {
        return true;
      }
      final int vilain2Index = onePlayerIndexer.indexOf(new int[][] {cardsGroups[2]});
      if (vilain2Index < vilain1Index) {
        return true;
      }
      final int indexInTables = threePlayersIndexer.indexOf(cardsGroups);
      final double[][] eq = equities[indexInTables];
      final double[][] dest =
          reducedEquities[heroIndex][vilain1Index][vilain2Index] = new double[4][3];
      for (int i = 0; i < 4; i++) {
        final double[] destI = dest[i];
        final double[] resultsI = eq[i];
        for (int j = 0; j < 3; j++) {
          destI[j] += resultsI[j];
        }
      }
      return true;
    });
    for (int i = 0; i < nbHoleCards; i++) {
      final double[][][][] heroEquities = reducedEquities[i];
      for (int j = i; j < nbHoleCards; j++) {
        final double[][][] heroVilain1Equities = heroEquities[j];
        for (int k = j; k < nbHoleCards; k++) {
          final double[][] heroVilain1Vilain2Equities = heroVilain1Equities[k];
          for (int l = 0; l < 4; l++) {
            final double[] eq = heroVilain1Vilain2Equities[l];
            final double total = eq[0] + eq[1] + eq[2];
            for (int m = 0; m < 3; m++) {
              eq[m] /= total;
            }
          }
        }
      }
    }
    computed = true;
  }

  public final void expand() {
    checkArgument(computed, "Tables must be computed before expanding");
    if (expanded) {
      log.warn("Tables are already expanded");
      return;
    }
    final int nbHoleCards = this.nbHoleCards;
    final double[][][][][] reducedEquities = this.reducedEquities;
    for (int i = 0; i < nbHoleCards; i++) {
      final double[][][][] heroEquities = reducedEquities[i];
      for (int j = 0; j < nbHoleCards; j++) {
        final double[][][] heroVilain1Equities = heroEquities[j];
        for (int k = 0; k < nbHoleCards; k++) {
          if (i <= j && j <= k) {
            continue;
          }
          final int[] ordered = getOrdered(i, j, k);
          final Permutator permutator = getPermutator(i, j, k);
          final double[][] heroVilain1Vilain2Equities = heroVilain1Equities[k] = new double[4][3];
          permutator.permuteInverse(reducedEquities[ordered[0]][ordered[1]][ordered[2]],
              heroVilain1Vilain2Equities);
        }
      }
    }
    expanded = true;
  }

  private static final int[] getOrdered(final int i, final int j, final int k) {
    if (i <= j && j <= k) {
      return new int[] {i, j, k};
    }
    if (i <= k && k <= j) {
      return new int[] {i, k, j};
    }
    if (j <= i && i <= k) {
      return new int[] {j, i, k};
    }
    if (j <= k && k <= i) {
      return new int[] {j, k, i};
    }
    if (k <= i && i <= j) {
      return new int[] {k, i, j};
    }
    if (k <= j && j <= i) {
      return new int[] {k, j, i};
    }
    throw new IllegalArgumentException();
  }

  public final double[][] getEquities(final int hand1, final int hand2, final int hand3) {
    if (expanded) {
      return reducedEquities[hand1][hand2][hand3];
    }
    final int[] ordered = getOrdered(hand1, hand2, hand3);
    final Permutator permutator = getPermutator(hand1, hand2, hand3);
    final double[][] res = new double[4][3];
    permutator.permuteInverse(reducedEquities[ordered[0]][ordered[1]][ordered[2]], res);
    return res;
  }

  @Override
  public void fill(InputStream is) throws IOException {
    final double[][][][][] reducedEquities = this.reducedEquities;
    final int nbHoleCards = this.nbHoleCards;
    for (int i = 0; i < nbHoleCards; i++) {
      final double[][][][] ei = reducedEquities[i];
      for (int j = i; j < nbHoleCards; j++) {
        final double[][][] eij = ei[j];
        for (int k = j; k < nbHoleCards; k++) {
          final double[][] eijk = eij[k] = new double[4][3];
          IOUtils.fill(is, eijk);
        }
      }
    }
    computed = true;
  }

  @Override
  public void write(OutputStream os) throws IOException {
    checkArgument(computed, "Tables are not computed");
    final double[][][][][] reducedEquities = this.reducedEquities;
    final int nbHoleCards = this.nbHoleCards;
    for (int i = 0; i < nbHoleCards; i++) {
      final double[][][][] ei = reducedEquities[i];
      for (int j = i; j < nbHoleCards; j++) {
        final double[][][] eij = ei[j];
        for (int k = j; k < nbHoleCards; k++) {
          final double[][] eijk = eij[k];
          IOUtils.write(os, eijk);
        }
      }
    }
  }

  public void interactiveCheck() {
    checkArgument(computed, "Tables must be computed");
    final Cards52Strings strs = new Cards52Strings(holeCardsIndexer.getCardsSpec());
    try (final Scanner scanner = new Scanner(System.in)) {
      while (true) {
        log.info(
            "Type 3 exact preflop hands to get their reduced equity. eg : \"AhAc KdQc 6s5s\" will output the equities for \"AA KQà 65s\"");
        final String line = scanner.nextLine();
        if (line.equals("exit")) {
          return;
        }
        try {
          final String[] handsStr = line.split(" ");
          final int[][] c1 = new int[][] {strs.getCards(handsStr[0])};
          final int[][] c2 = new int[][] {strs.getCards(handsStr[1])};
          final int[][] c3 = new int[][] {strs.getCards(handsStr[2])};
          final int i1 = holeCardsIndexer.indexOf(c1);
          final int i2 = holeCardsIndexer.indexOf(c2);
          final int i3 = holeCardsIndexer.indexOf(c3);
          log.info("{}", (Object[]) reducedEquities[i1][i2][i3]);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {

    checkArgument(args.length == 2,
        "3 Players Preflop Tables writing misses a path argument, expected source and destination");
    final String pathStr = args[0];
    final Path destPath = Paths.get(args[1]);
    final Path srcPath = Paths.get(pathStr);
    checkArgument(Files.exists(srcPath),
        "File " + srcPath.toAbsolutePath().toString() + " doesn't exist");
    checkArgument(!Files.exists(destPath),
        "File " + destPath.toAbsolutePath().toString() + " already exists");

    final ThreePlayersPreflopEquityTable fullTables = new ThreePlayersPreflopEquityTable();
    log.info("Filling exact equities table");
    try (final FileInputStream fis = new FileInputStream(srcPath.toFile())) {
      fullTables.fill(fis);
    } catch (IOException e1) {
      e1.printStackTrace();
      System.exit(-1);
    }
    log.info("Computing reduced equities");
    final ThreePlayersPreflopReducedEquityTable table = new ThreePlayersPreflopReducedEquityTable();
    table.compute(fullTables);
    log.info("Writing reduced equities");
    try (final FileOutputStream fos = new FileOutputStream(destPath.toFile())) {
      table.write(fos);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}

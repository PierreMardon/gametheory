package net.funkyjava.gametheory.games.nlhe.flop;

import static net.funkyjava.gametheory.io.ProgramArguments.getArgument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.cscfrm.CSCFRMChancesSynchronizer;
import net.funkyjava.gametheory.cscfrm.CSCFRMData;
import net.funkyjava.gametheory.cscfrm.CSCFRMMutexChancesSynchronizer;
import net.funkyjava.gametheory.cscfrm.CSCFRMRunner;
import net.funkyjava.gametheory.games.nlhe.HoldEm;
import net.funkyjava.gametheory.games.nlhe.preflop.HEPreflopExcel;
import net.funkyjava.gametheory.games.nlhe.preflop.HEPreflopHelper;
import net.funkyjava.gametheory.gameutil.clustering.Buckets;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHandParser;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLAbstractedBetTree;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLBetTreeAbstractor;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLBetTreeNode;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLPushFoldBetTreeAbstractor;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.ThreePlayersPreflopReducedEquityTable;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh.WaughIndexer;

@Slf4j
public class HE3PlayersFlopCSCFRM {

  public static final String equityPathPrefix = "equity=";
  public static final String handPrefix = "hand=";
  public static final String svgPathPrefix = "svg=";
  public static final String interactiveArg = "-i";
  private static final String betTreePathPrefix = "tree=";
  private static final String flopBuckets = "flopBuckets=";

  private static ThreePlayersPreflopReducedEquityTable getTables(final String path)
      throws IOException {
    try (final FileInputStream fis = new FileInputStream(Paths.get(path).toFile())) {
      final ThreePlayersPreflopReducedEquityTable res = new ThreePlayersPreflopReducedEquityTable();
      res.fill(fis);
      res.expand();
      return res;
    }
  }

  public static void main(String[] args) throws IOException {
    final Optional<String> handOpt = getArgument(args, handPrefix);
    if (!handOpt.isPresent()) {
      log.error("Unable to parse hand settings");
      return;
    }
    final NLHand hand = NLHandParser.parse(handOpt.get(), 2);
    final Optional<String> eqOpt = getArgument(args, equityPathPrefix);
    if (!eqOpt.isPresent()) {
      return;
    }
    log.info("Loading equity tables");
    ThreePlayersPreflopReducedEquityTable tables;
    try {
      tables = getTables(eqOpt.get());
    } catch (Exception e) {
      log.error("Unable to load 3 players preflop equity tables", e);
      return;
    }
    final Optional<String> bucketsPath = getArgument(args, flopBuckets);
    log.info("Loading flop buckets");
    if (!bucketsPath.isPresent()) {
      return;
    }
    final Buckets flopBuckets = new Buckets();
    flopBuckets.fill(bucketsPath.get());
    log.info("Loaded flop buckets with index size {}, nb buckets {}",
        flopBuckets.getBuckets().length, flopBuckets.getNbBuckets());
    final Optional<String> svgOpt = getArgument(args, svgPathPrefix);
    log.info("Creating CSCFRM environment");
    final Optional<String> betTreeOpt = getArgument(args, betTreePathPrefix);
    HE3PlayersFlopCSCFRM cfrmTmp;
    if (betTreeOpt.isPresent()) {
      final NLBetTreeAbstractor abstractor = NLBetTreeAbstractor.read(betTreeOpt.get());
      cfrmTmp = new HE3PlayersFlopCSCFRM(hand, abstractor, tables, svgOpt.orNull(), flopBuckets);
    } else {
      cfrmTmp = new HE3PlayersFlopCSCFRM(hand, tables, svgOpt.orNull(), flopBuckets);
    }
    final HE3PlayersFlopCSCFRM cfrm = cfrmTmp;
    try {
      cfrm.load();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    log.info("Adding shutdown hook to save the data on gentle kill");
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutting down");
      try {
        if (cfrm.runner.isRunning()) {
          log.info("Waiting runner termination");
          cfrm.runner.stopAndAwaitTermination();
          log.info("Saving...");
          cfrm.save();
        }
        cfrm.printStrategies();
      } catch (InterruptedException | IOException e) {
        e.printStackTrace();
      }
    }));
    if (getArgument(args, interactiveArg).isPresent()) {
      log.info("Interactive mode");
      try {
        interactive(cfrm);
      } catch (Exception e) {

      }
    } else {
      log.info("Non-Interactive mode, running CSCFRM");
      cfrm.runner.start();
      try {
        log.info(
            "Trying to read on standard input. Failure will let run, on success hitting Enter will stop and save.");
        System.in.read();
        System.exit(0);
      } catch (Exception e) {

      }
    }
  }

  private static final void interactive(final HE3PlayersFlopCSCFRM cscfrm)
      throws InterruptedException, IOException {
    try (final Scanner scan = new Scanner(System.in);) {
      while (true) {
        log.info("Enter one of those commands : run | stop | print | exit");
        final String line = scan.nextLine();
        switch (line) {
          case "run":
            if (cscfrm.runner.isRunning()) {
              log.info("Already running");
              continue;
            }
            cscfrm.runner.start();
            break;
          case "stop":
            if (!cscfrm.runner.isRunning()) {
              log.info("Not running");
              continue;
            }
            cscfrm.runner.stopAndAwaitTermination();
            cscfrm.save();
            break;
          case "print":
            if (cscfrm.runner.isRunning()) {
              log.info("Can't print strategies while running");
              continue;
            }
            cscfrm.printStrategies();
            break;
          case "exit":
            System.exit(0);
            return;
          default:
            log.info("Unknown command \"{}\"", line);
        }
      }
    }
  }

  @Getter
  private final CSCFRMData<NLBetTreeNode, HEFlopChances> data;
  @Getter
  private final CSCFRMRunner<HEFlopChances> runner;
  private final ThreePlayersPreflopReducedEquityTable tables;
  private final String svgPath;
  @Getter
  private final WaughIndexer holeCardsIndexer;

  public HE3PlayersFlopCSCFRM(final NLHand hand, final NLBetTreeAbstractor betTreeAbstractor,
      final ThreePlayersPreflopReducedEquityTable tables, final String svgPath,
      final Buckets flopBuckets) {
    this.tables = tables;
    this.svgPath = svgPath;
    this.holeCardsIndexer = tables.getHoleCardsIndexer();
    final HE3PlayersFlopEquityProvider equityProvider = new HE3PlayersFlopEquityProvider(tables);
    final NLAbstractedBetTree tree = new NLAbstractedBetTree(hand, betTreeAbstractor, false);
    Preconditions.checkArgument(tree.nbOfBetRounds <= 2,
        "The bet tree should not have more than two bet rounds");
    final HoldEm<HEFlopChances> game =
        new HoldEm<>(tree, new int[] {169, flopBuckets.getNbBuckets()}, equityProvider);
    final HEFlopChancesProducer chancesProducer =
        new HEFlopChancesProducer(3, flopBuckets.getBuckets());
    final int nbBuckets = flopBuckets.getNbBuckets();
    final int[][] chancesSizes = new int[][] {{169, 169, 169}, {nbBuckets, nbBuckets, nbBuckets}};
    final CSCFRMChancesSynchronizer<HEFlopChances> synchronizer =
        new CSCFRMMutexChancesSynchronizer<>(chancesProducer, chancesSizes);
    final CSCFRMData<NLBetTreeNode, HEFlopChances> data = this.data = new CSCFRMData<>(game);
    final int nbTrainerThreads = Math.max(Runtime.getRuntime().availableProcessors(), 1);
    this.runner = new CSCFRMRunner<>(data, synchronizer, nbTrainerThreads);
  }

  public HE3PlayersFlopCSCFRM(final NLHand hand, final ThreePlayersPreflopReducedEquityTable tables,
      final String svgPath, final Buckets flopBuckets) {
    this(hand, new NLPushFoldBetTreeAbstractor(), tables, svgPath, flopBuckets);
  }

  private void load() throws IOException {
    if (svgPath == null) {
      log.warn("No svg path provided, not loading");
      return;
    }
    final File file = Paths.get(svgPath).toFile();
    if (!file.exists()) {
      log.warn("No file at path {}, may be initial run", svgPath);
      return;
    }
    try (final FileInputStream fis = new FileInputStream(file)) {
      data.fill(fis);
    } catch (IOException e) {
      log.error("Failed to load file at path {}", svgPath);
      throw e;
    }
  }

  private void save() throws IOException {
    if (svgPath == null) {
      log.warn("No svg path provided, not saving");
      return;
    }
    final File file = Paths.get(svgPath).toFile();
    if (file.exists()) {
      if (!file.delete()) {
        log.error("Failed to delete file at path {}, cannot save", svgPath);
        return;
      }
    }
    try (final FileOutputStream fos = new FileOutputStream(file)) {
      data.write(fos);
    } catch (IOException e) {
      log.error("Failed to save file at path {}", svgPath);
      throw e;
    }
  }

  final static Map<Integer, String> getPlayersNames() {
    final Map<Integer, String> playersNames = new HashMap<>();
    playersNames.put(0, "SB");
    playersNames.put(1, "BB");
    playersNames.put(2, "BT");
    return playersNames;
  }

  public void printStrategies() {
    HEPreflopHelper.printStrategies(data, holeCardsIndexer, getPlayersNames());
  }

  public void writeStrategiesExcel(String pathStr) {
    try {
      final Workbook wb = HEPreflopExcel.createStrategiesWorkBook(data,
          tables.getHoleCardsIndexer(), getPlayersNames());
      try (final FileOutputStream fos = new FileOutputStream(pathStr)) {
        wb.write(fos);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      log.error("error ", e);
    }
  }

  public void writeStrategiesSheet(final Map<String, CellStyle> styles, final String sheetName,
      final Workbook wb) {
    HEPreflopExcel.createStrategiesSheet(sheetName, wb, data, holeCardsIndexer, getPlayersNames());
  }
}

package net.funkyjava.gametheory.games.nlhe.flop;

import static net.funkyjava.gametheory.io.ProgramArguments.getArgument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.clustering.Buckets;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHandParser;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLAbstractedBetTree;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLBetTreeAbstractor;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLBetTreeNode;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLPushFoldBetTreeAbstractor;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.HUPreflopEquityTables;

@Slf4j
public class HEHUFlopCSCFRM {

  private static final String equityPathPrefix = "equity=";
  private static final String svgPathPrefix = "svg=";
  private static final String interactiveArg = "-i";
  private static final String handPrefix = "hand=";
  private static final String betTreePathPrefix = "tree=";
  private static final String flopBuckets = "flopBuckets=";

  private static HUPreflopEquityTables getTables(final String path)
      throws IOException, ClassNotFoundException {
    try (final FileInputStream fis = new FileInputStream(Paths.get(path).toFile());
        final ObjectInputStream objectInputStream = new ObjectInputStream(fis)) {
      final HUPreflopEquityTables tables = (HUPreflopEquityTables) objectInputStream.readObject();
      return tables;
    }
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {
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
    HUPreflopEquityTables tables;
    try {
      tables = getTables(eqOpt.get());
    } catch (Exception e) {
      log.error("Unable to load HU preflop equity tables", e);
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
    HEHUFlopCSCFRM cfrmTmp;
    if (betTreeOpt.isPresent()) {
      final NLBetTreeAbstractor abstractor = NLBetTreeAbstractor.read(betTreeOpt.get());
      cfrmTmp = new HEHUFlopCSCFRM(hand, abstractor, tables, svgOpt.orNull(), flopBuckets);
    } else {
      cfrmTmp = new HEHUFlopCSCFRM(hand, tables, svgOpt.orNull(), flopBuckets);
    }
    final HEHUFlopCSCFRM cfrm = cfrmTmp;
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
        cfrm.printPreflopStrategies();
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

  private static final void interactive(final HEHUFlopCSCFRM cscfrm)
      throws InterruptedException, IOException {
    try (final Scanner scan = new Scanner(System.in);) {
      while (true) {
        log.info(
            "Enter one of those commands : run | stop | print | exit | write /path/to/File.xlsx");
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
            cscfrm.printPreflopStrategies();
            break;
          case "exit":
            System.exit(0);
            return;
          default:
            if (!handleWriteCommand(line, cscfrm)) {
              log.info("Unknown command \"{}\"", line);
            }
        }
      }
    }
  }

  private static final boolean handleWriteCommand(final String cmd, final HEHUFlopCSCFRM cscfrm) {
    final String[] splitted = cmd.split(" ");
    if (!splitted[0].trim().equals("write")) {
      return false;
    }
    if (splitted.length != 2) {
      log.warn("Expected a cmd with exactly one space : \"write /home/pitt/MyFile.xlsx\"");
      return true;
    }
    final String pathStr = splitted[1];
    cscfrm.writePreflopStrategiesExcel(pathStr);
    return true;
  }

  private final HUPreflopEquityTables tables;
  @Getter
  private final CSCFRMData<NLBetTreeNode, HEFlopChances> data;
  @Getter
  private final CSCFRMRunner<HEFlopChances> runner;
  private final String svgPath;

  public HEHUFlopCSCFRM(final NLHand hand, final NLBetTreeAbstractor betTreeAbstractor,
      final HUPreflopEquityTables tables, final String svgPath, final Buckets flopBuckets) {
    this.tables = tables;
    this.svgPath = svgPath;
    final HEHUFlopEquityProvider equityProvider =
        new HEHUFlopEquityProvider(tables, DefaultIntCardsSpecs.getDefault());
    final NLAbstractedBetTree tree = new NLAbstractedBetTree(hand, betTreeAbstractor, false);
    Preconditions.checkArgument(tree.nbOfBetRounds <= 2,
        "The bet tree should not have more than two bet rounds");
    final int nbFlopBuckets = flopBuckets.getNbBuckets();
    final HoldEm<HEFlopChances> game =
        new HoldEm<>(tree, new int[] {169, nbFlopBuckets}, equityProvider);
    final HEFlopChancesProducer chancesProducer =
        new HEFlopChancesProducer(2, flopBuckets.getBuckets());
    final int[][] chancesSizes = new int[][] {{169, 169}, {nbFlopBuckets, nbFlopBuckets}};
    final CSCFRMChancesSynchronizer<HEFlopChances> synchronizer =
        new CSCFRMMutexChancesSynchronizer<>(chancesProducer, chancesSizes);
    final CSCFRMData<NLBetTreeNode, HEFlopChances> data = this.data = new CSCFRMData<>(game);
    final int nbTrainerThreads = Math.max(Runtime.getRuntime().availableProcessors(), 1);
    this.runner = new CSCFRMRunner<>(data, synchronizer, nbTrainerThreads);
  }

  public HEHUFlopCSCFRM(final NLHand hand, final HUPreflopEquityTables tables, final String svgPath,
      final Buckets flopBuckets) {
    this(hand, new NLPushFoldBetTreeAbstractor(), tables, svgPath, flopBuckets);
  }

  public void load() throws IOException {
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

  public void save() throws IOException {
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

  private static Map<Integer, String> getPlayersNames() {
    final Map<Integer, String> playersNames = new HashMap<>();
    playersNames.put(0, "SB");
    playersNames.put(1, "BB");
    return playersNames;
  }

  public void printPreflopStrategies() {
    HEPreflopHelper.printStrategies(data, tables.getHoleCardsIndexer(), getPlayersNames());
  }

  public void writePreflopStrategiesExcel(String pathStr) {
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

}

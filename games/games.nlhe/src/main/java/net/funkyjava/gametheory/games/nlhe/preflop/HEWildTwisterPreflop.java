package net.funkyjava.gametheory.games.nlhe.preflop;

import static net.funkyjava.gametheory.io.ProgramArguments.getArgument;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.google.common.base.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.BetRoundSpec;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.BlindsAnteSpec;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.NoBetPlayerData;
import net.funkyjava.gametheory.gameutil.poker.he.evaluators.ThreePlayersPreflopReducedEquityTable;
import net.funkyjava.gametheory.io.ProgramArguments;

@Slf4j
public class HEWildTwisterPreflop {

  public static final String workbookPathPrefix = "excel=";

  final private int sb = 5;
  final private int bb = 10;
  final private int initStack = 50;
  final private int totalChips = initStack * 3;
  final private int nbStacks = totalChips / sb - 1;
  final HE3PlayersPreflopCSCFRM[][][] cscfrms =
      new HE3PlayersPreflopCSCFRM[nbStacks][nbStacks][nbStacks];

  private final int createRunners(final ThreePlayersPreflopReducedEquityTable table) {
    int res = 0;
    final BetRoundSpec betsSpec = new BetRoundSpec(0, bb);
    final BlindsAnteSpec blindsSpecs =
        new BlindsAnteSpec(false, true, false, sb, bb, 0, Collections.emptyList(), 0, 1);
    final int sb = this.sb;
    final int nbStacks = this.nbStacks;
    final int totalChips = this.totalChips;
    for (int i = 1; i < nbStacks; i++) {
      final int iStack = i * sb;
      final NoBetPlayerData sbData = new NoBetPlayerData(0, iStack, true);
      for (int j = 1; j < nbStacks; j++) {
        final int jStack = j * sb;
        final NoBetPlayerData bbData = new NoBetPlayerData(1, jStack, true);
        for (int k = 1; k < nbStacks; k++) {
          final int kStack = k * sb;
          if (iStack + jStack + kStack != totalChips) {
            continue;
          }
          res++;
          final List<NoBetPlayerData> playersData = new LinkedList<>();

          final NoBetPlayerData btData = new NoBetPlayerData(2, kStack, true);
          playersData.add(sbData);
          playersData.add(bbData);
          playersData.add(btData);
          final NLHand hand = new NLHand(playersData, blindsSpecs, betsSpec, 1);
          cscfrms[i][j][k] = new HE3PlayersPreflopCSCFRM(hand, table, null);
        }
      }
    }
    return res;
  }

  public static interface WildTwisterHandler {
    void handle(final int sbStack, final int bbStack, final int btStack,
        final HE3PlayersPreflopCSCFRM cscfrm) throws Exception;
  }

  public static class WildTwisterPrinter implements WildTwisterHandler {
    @Override
    public void handle(int sbStack, int bbStack, int btStack, HE3PlayersPreflopCSCFRM cscfrm) {
      log.info("\n\n");
      log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
      log.info("{} - {} - {}", sbStack, bbStack, btStack);
      log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
      cscfrm.printStrategies();
    }
  }

  public static class WildTwisterWorkbookPrinter implements WildTwisterHandler {

    @Getter
    private final Workbook workbook = new SXSSFWorkbook();
    private final Map<String, CellStyle> styles = HEPreflopExcel.createStyles(workbook);

    @Override
    public void handle(int sbStack, int bbStack, int btStack, HE3PlayersPreflopCSCFRM cscfrm) {
      cscfrm.writeStrategiesSheet(styles, sbStack + " " + bbStack + " " + btStack, workbook);
    }
  }

  @AllArgsConstructor
  public static class WildTwisterTimedRunner implements WildTwisterHandler {

    private final long timeToWait;

    @Override
    public void handle(int sbStack, int bbStack, int btStack, HE3PlayersPreflopCSCFRM cscfrm)
        throws Exception {
      synchronized (this) {
        cscfrm.getRunner().start();
        this.wait(timeToWait);
        cscfrm.getRunner().stopAndAwaitTermination();
      }
    }

  }

  private final void forEachCSCFRM(final WildTwisterHandler handler) throws Exception {
    final int nbStacks = this.nbStacks;
    final int totalChips = this.totalChips;
    for (int i = 1; i < nbStacks; i++) {
      final int iStack = i * sb;
      for (int j = 1; j < nbStacks; j++) {
        final int jStack = j * sb;
        for (int k = 1; k < nbStacks; k++) {
          final int kStack = k * sb;
          if (iStack + jStack + kStack != totalChips) {
            continue;
          }
          final HE3PlayersPreflopCSCFRM cscfrm = cscfrms[i][j][k];
          handler.handle(iStack, jStack, kStack, cscfrm);
        }
      }
    }
  }

  private final void runEachFor(final long milliseconds) throws Exception {
    forEachCSCFRM(new WildTwisterTimedRunner(milliseconds));
  }

  private static ThreePlayersPreflopReducedEquityTable getTables(final String path)
      throws IOException {
    try (final FileInputStream fis = new FileInputStream(Paths.get(path).toFile())) {
      final ThreePlayersPreflopReducedEquityTable res = new ThreePlayersPreflopReducedEquityTable();
      res.fill(fis);
      res.expand();
      return res;
    }
  }

  public static void main(String[] args) throws InterruptedException {
    final Optional<String> eqOpt = getArgument(args, HE3PlayersPreflopCSCFRM.equityPathPrefix);
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
    final HEWildTwisterPreflop wtp = new HEWildTwisterPreflop();
    final int nbHands = wtp.createRunners(tables);
    log.info("Nb hands : {}", nbHands);
    final Optional<String> excelOpt = ProgramArguments.getArgument(args, workbookPathPrefix);
    if (excelOpt.isPresent()) {
      try (final FileOutputStream fos = new FileOutputStream(excelOpt.get())) {
        log.info("Running for {} ms per runner", 1000);
        wtp.runEachFor(1000);
        log.info("Printing to XLSX {}", excelOpt.get());
        final WildTwisterWorkbookPrinter printer = new WildTwisterWorkbookPrinter();
        wtp.forEachCSCFRM(printer);
        printer.getWorkbook().write(fos);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      try {
        wtp.runEachFor(3000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}

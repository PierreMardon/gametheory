package net.funkyjava.gametheory.games.nlhe.preflop;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import net.funkyjava.gametheory.cscfrm.CSCFRMData;
import net.funkyjava.gametheory.cscfrm.CSCFRMNode;
import net.funkyjava.gametheory.extensiveformgame.LinkedActionTreeNode;
import net.funkyjava.gametheory.gameutil.cards.indexing.CardsGroupsIndexer;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLBetTreeNode;

/**
 * Preflop hold'em excel utilities, meant to produce strategies matrix
 * 
 * @author Pierre Mardon
 *
 */
public class HEPreflopExcel {

  private final static String redStyle = "RED";
  private final static String greenStyle = "GREEN";
  private final static String yellowStyle = "YELLOW";
  private final static String orangeStyle = "ORANGE";
  private final static String gameTitleStyle = "GAME_TITLE";
  private final static String handStateStyle = "HAND_STATE";
  private final static String moveTitleStyle = "MOVE_TITLE";

  /**
   * Create a single sheet XLSX workbook for preflop strategies
   * 
   * @param data the CSCFRM data
   * @param holeCardsIndexer the hole cards indexer matching how CSCFRM data is indexed
   * @param playersNames the players names mapping, players names are not mandatory
   * @return the created workbook
   */
  public static Workbook createStrategiesWorkBook(final CSCFRMData<NLBetTreeNode, ?> data,
      final CardsGroupsIndexer holeCardsIndexer, final Map<Integer, String> playersNames) {
    final Workbook wb = new SXSSFWorkbook();
    final Map<String, CellStyle> styles = createStyles(wb);
    createStrategiesSheet(styles, "All Strategies", wb, data, holeCardsIndexer, playersNames);
    return wb;
  }

  /**
   * Create a new strategy sheet
   * 
   * @param sheetName name of the sheet
   * @param wb the workbook to append the sheet to
   * @param data the CSCFRM data
   * @param holeCardsIndexer the hole cards indexer matching how CSCFRM data is indexed
   * @param playersNames the players names mapping, players names are not mandatory
   */
  public static void createStrategiesSheet(final String sheetName, final Workbook wb,
      final CSCFRMData<NLBetTreeNode, ?> data, final CardsGroupsIndexer holeCardsIndexer,
      final Map<Integer, String> playersNames) {
    createStrategiesSheet(createStyles(wb), sheetName, wb, data, holeCardsIndexer, playersNames);

  }

  /**
   * Create a strategy sheet
   * 
   * @param styles the cell styles mapping that should be got from {@link #createStyles(Workbook)}
   *        or created using the same keys
   * @param sheetName the sheet name
   * @param wb the workbook to append the sheet to
   * @param data the CSCFRM data
   * @param holeCardsIndexer the hole cards indexer matching how CSCFRM data is indexed
   * @param playersNames the players names mapping, players names are not mandatory
   */
  public static void createStrategiesSheet(final Map<String, CellStyle> styles,
      final String sheetName, final Workbook wb, final CSCFRMData<NLBetTreeNode, ?> data,
      final CardsGroupsIndexer holeCardsIndexer, final Map<Integer, String> playersNames) {
    final Sheet sheet = wb.createSheet(sheetName);
    sheet.createDrawingPatriarch();
    int rowIndex = 0;
    final Row titleRow = sheet.createRow(rowIndex);
    final CellRangeAddress cellRangeAddress = new CellRangeAddress(rowIndex, rowIndex, 0, 13);
    sheet.addMergedRegion(cellRangeAddress);
    final Cell titleCell = titleRow.createCell(0);
    titleCell.setCellStyle(styles.get(gameTitleStyle));
    titleCell.setCellValue(sheetName);
    rowIndex++;
    final Map<LinkedActionTreeNode<NLBetTreeNode, ?>, CSCFRMNode[]> allNodes =
        data.nodesForEachActionNode();
    for (LinkedActionTreeNode<NLBetTreeNode, ?> actionNode : allNodes.keySet()) {
      if (actionNode.getPlayerNode().getRound() != 0) {
        // Preflop only
        continue;
      }
      final CSCFRMNode[] nodes = allNodes.get(actionNode);
      final Map<Move, double[][]> strats =
          HEPreflopHelper.getMovesStrategies(actionNode, nodes, holeCardsIndexer);
      rowIndex += writeMovesStrats(sheet, playersNames, actionNode.getPlayerNode().getId(), styles,
          strats, rowIndex);
    }
  }

  private static int writeMovesStrats(final Sheet sheet, final Map<Integer, String> playersNames,
      final NLBetTreeNode node, final Map<String, CellStyle> styles,
      final Map<Move, double[][]> strats, final int startRowIndex) {
    int rowIndex = startRowIndex;
    final Row titleRow = sheet.createRow(rowIndex);
    final CellRangeAddress cellRangeAddress = new CellRangeAddress(rowIndex, rowIndex, 0, 13);
    sheet.addMergedRegion(cellRangeAddress);
    final Cell titleCell = titleRow.createCell(0);
    titleCell.setCellStyle(styles.get(handStateStyle));
    titleCell.setCellValue(node.getHand().movesString(playersNames));
    rowIndex++;
    for (Move move : strats.keySet()) {
      final Row moveRow = sheet.createRow(rowIndex);
      final Cell moveCell = moveRow.createCell(0);
      moveCell.setCellStyle(styles.get(moveTitleStyle));
      String playerName = playersNames.get(move.getPlayer());
      if (playerName == null) {
        playerName = "" + move.getPlayer();
      }
      moveCell.setCellValue(playerName + " " + move.toString());
      final CellRangeAddress cellRangeAddressMove = new CellRangeAddress(rowIndex, rowIndex, 0, 13);
      sheet.addMergedRegion(cellRangeAddressMove);
      rowIndex++;
      rowIndex += writeStrat(sheet, styles, strats.get(move), rowIndex, 0);
      rowIndex++;
    }
    return rowIndex - startRowIndex;
  }

  private static int writeStrat(final Sheet sheet, final Map<String, CellStyle> styles,
      final double[][] strat, final int startRow, final int startColumn) {
    final String[][] handsStrings = HEPreflopHelper.canonicalPreflopHandNames;
    final Drawing drawing = sheet.getDrawingPatriarch();
    final int nbRows = handsStrings.length;
    final int nbColumns = nbRows;
    for (int i = 0; i < nbRows; i++) {
      final int rowIndex = startRow + i;
      final Row row = sheet.createRow(rowIndex);
      for (int j = 0; j < nbColumns; j++) {
        final int columnIndex = j + startColumn;
        final Cell cell = row.createCell(columnIndex);
        final double val = strat[i][j];
        CellStyle style;
        if (val < 0.25) {
          style = styles.get(redStyle);
        } else if (val < 0.5) {
          style = styles.get(orangeStyle);
        } else if (val < 0.75) {
          style = styles.get(yellowStyle);
        } else {
          style = styles.get(greenStyle);
        }
        cell.setCellStyle(style);
        cell.setCellValue(handsStrings[i][j]);
        final int percent = (int) (val * 100);
        final ClientAnchor anchor =
            drawing.createAnchor(0, 0, 0, 0, columnIndex, rowIndex, columnIndex + 1, rowIndex + 1);
        final Comment comment = drawing.createCellComment(anchor);
        comment.setString(new XSSFRichTextString(String.format("%d%%", percent)));
        cell.setCellComment(comment);
      }
    }
    return nbRows;
  }

  /**
   * Creates default cell styles mapping
   * 
   * @param wb the workbook in which the styles will be used
   * @return the cell styles mapping
   */
  public static Map<String, CellStyle> createStyles(Workbook wb) {
    Map<String, CellStyle> styles = new HashMap<>();

    Font handFont = wb.createFont();
    handFont.setFontHeightInPoints((short) 20);
    handFont.setColor(IndexedColors.BLACK.getIndex());

    CellStyle style;
    style = wb.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setFont(handFont);
    style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    styles.put(greenStyle, style);

    style = wb.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setFont(handFont);
    style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    styles.put(yellowStyle, style);

    style = wb.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setFont(handFont);
    style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    styles.put(orangeStyle, style);

    style = wb.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setFont(handFont);
    style.setFillForegroundColor(IndexedColors.RED.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    styles.put(redStyle, style);

    Font gameTitleFont = wb.createFont();
    gameTitleFont.setFontHeightInPoints((short) 60);
    gameTitleFont.setColor(IndexedColors.WHITE.getIndex());
    style = wb.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setFont(gameTitleFont);
    style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    styles.put(gameTitleStyle, style);

    Font handStateTitleFont = wb.createFont();
    handStateTitleFont.setFontHeightInPoints((short) 17);
    handStateTitleFont.setColor(IndexedColors.WHITE.getIndex());
    style = wb.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setFont(handStateTitleFont);
    style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    styles.put(handStateStyle, style);

    Font moveFont = wb.createFont();
    moveFont.setFontHeightInPoints((short) 35);
    moveFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
    style = wb.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setFont(moveFont);
    style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    styles.put(moveTitleStyle, style);

    return styles;
  }
}

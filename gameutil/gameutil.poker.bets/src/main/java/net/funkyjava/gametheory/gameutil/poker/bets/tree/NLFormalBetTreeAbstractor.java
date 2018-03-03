package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.google.common.base.Optional;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.Pot;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetRange;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.RaiseRange;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayerData;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.ParsedMove.ParsedMoveType;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.ParsedMove.ParsedMovesNode;

@Slf4j
public class NLFormalBetTreeAbstractor implements NLBetTreeAbstractor {

  private final ParsedMovesNode root;

  public NLFormalBetTreeAbstractor(final ParsedMovesNode root) {
    this.root = root;
  }

  public static NLFormalBetTreeAbstractor read(final InputStream is) {
    final ParsedMovesNode root = new ParsedMovesNode();
    try (final Scanner scanner = new Scanner(is);) {
      List<ParsedMove> lastSequence = new ArrayList<>();
      int lineNb = 0;
      int lastLineParsed = 0;
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine();
        if (line.trim().isEmpty()) {
          lineNb++;
          continue;
        }
        final String[] splitted = line.split("[\t ]*-[\t ]*");
        final int counts = splitted.length;
        List<ParsedMove> newSequence = new ArrayList<>();
        boolean sawFirstValue = false;
        for (int i = 0; i < counts; i++) {
          final String val = splitted[i].trim();
          if (val.isEmpty()) {
            checkArgument(!sawFirstValue,
                "Unexpected empty value tabulation index " + i + " on line index " + lineNb);
            checkArgument(lastSequence.size() > i, "Unable to infer value from last parsed line "
                + lastLineParsed + " in line " + lineNb + " because it is too short");
            newSequence.add(lastSequence.get(i));
          } else {
            sawFirstValue = true;
            try {
              newSequence.add(ParsedMove.parse(val));
            } catch (Exception e) {
              throw new IllegalArgumentException("Unable to parse value on tabulation index " + i
                  + " for line index " + lineNb + " from string \"" + val + "\"", e);
            }
          }
        }
        putSequence(newSequence, root);
        lastLineParsed = lineNb;
        lineNb++;
        lastSequence = newSequence;
      }
    }
    return new NLFormalBetTreeAbstractor(root);
  }

  public static NLFormalBetTreeAbstractor read(final Path path)
      throws FileNotFoundException, IOException {
    checkArgument(Files.exists(path), "No file at path " + path);
    try (final FileInputStream fis = new FileInputStream(path.toFile())) {
      return read(fis);
    }
  }

  public static NLFormalBetTreeAbstractor read(final String pathStr)
      throws FileNotFoundException, IOException {
    final Path path = Paths.get(pathStr);
    return read(path);
  }

  private final static void putSequence(final List<ParsedMove> sequence,
      final ParsedMovesNode root) {
    ParsedMovesNode node = root;
    for (ParsedMove val : sequence) {
      if (!node.children.containsKey(val)) {
        node.children.put(val, new ParsedMovesNode());
      }
      node = node.children.get(val);
    }
  }

  @Override
  public List<Move> movesForHand(final NLHand hand) {
    log.info("Getting moves for hand {}", hand);
    final ParsedMovesNode node = findNode(hand);
    final List<Move> result = new LinkedList<>();
    for (ParsedMove parsedMove : node.children.keySet()) {
      final Optional<Move> moveOpt = ParsedMove.moveFrom(hand, parsedMove);
      if (moveOpt.isPresent()) {
        final Move move = moveOpt.get();
        // Moves can be expressed in different formal ways, but we never want to distinguish them
        if (!result.contains(move)) {
          result.add(move);
        }
      }
    }
    return result;
  }



  private final ParsedMovesNode findNode(final NLHand hand) {
    final List<List<Move>> roundMoves = hand.getBetMoves();
    final List<Move> moves = new ArrayList<>();
    for (List<Move> rMoves : roundMoves) {
      moves.addAll(rMoves);
    }
    final List<NLHand> hands = hand.getPreviousPlayersBetMoveStates();
    checkArgument(hands.size() == moves.size(), "Not same number of hands before bet ("
        + hands.size() + ")/ bet moves (" + moves.size() + ")");
    final int length = hands.size();
    ParsedMovesNode node = root;
    for (int i = 0; i < length; i++) {
      node = findChildNode(hands.get(i), node, moves.get(i));
    }
    return node;
  }

  private final ParsedMovesNode findChildNode(final NLHand hand, final ParsedMovesNode node,
      final Move move) {
    log.info("Looking for child node for move {}", move);
    switch (move.getType()) {
      case SB:
      case NO_BLIND:
      case NO_ANTE:
      case ANTE:
      case BB:
        return node;
      case BET:
        return findBetNode(hand, node, move.getValue());
      case CALL:
        return findCallNode(node, move.getValue());
      case FOLD:
        return findFoldNode(node, move.getValue());
      case RAISE:
        return findRaiseNode(hand, node, move.getValue());
    }
    throw new IllegalStateException();
  }

  private final int getPotBet(final NLHand hand, final double multiplier) {
    int res = 0;
    final List<Pot> pots = hand.getCurrentPots();
    for (Pot pot : pots) {
      res += pot.getValue();
    }
    return (int) multiplier * res;
  }

  private final int getMaxBetBet(final NLHand hand, final double multiplier) {
    int maxBet = 0;
    final List<PlayerData> players = hand.getPlayersData();
    for (PlayerData player : players) {
      maxBet = Math.max(player.getBet(), maxBet);
    }
    return (int) multiplier * maxBet;
  }

  private final ParsedMovesNode findBetNode(final NLHand hand, final ParsedMovesNode node,
      final int betValue) {
    for (ParsedMove move : node.children.keySet()) {
      if (move.getType() == ParsedMoveType.NUMERIC && move.getNumericValue() == betValue) {
        return node.children.get(move);
      }
      if (move.getType() == ParsedMoveType.POT_MULTIPLIER
          && getPotBet(hand, move.getMultiplier()) == betValue) {
        return node.children.get(move);
      }
      if (move.getType() == ParsedMoveType.ALL_IN) {
        final BetRange betRange = hand.getBetChoice().getBetRange();
        if (betRange.exists() && betRange.getMax() == betValue) {
          return node.children.get(move);
        }
      }
    }
    throw new IllegalStateException("Couldn't find a bet node");
  }

  private final static ParsedMovesNode findCallNode(final ParsedMovesNode node,
      final int callValue) {
    for (ParsedMove move : node.children.keySet()) {
      if (move.getType() == ParsedMoveType.CALL) {
        return node.children.get(move);
      }
      if (move.getType() == ParsedMoveType.NUMERIC && callValue == move.getNumericValue()) {
        return node.children.get(move);
      }
    }
    throw new IllegalStateException("Couldn't find a call node");
  }

  private final static ParsedMovesNode findFoldNode(final ParsedMovesNode node,
      final int callValue) {
    for (ParsedMove move : node.children.keySet()) {
      if (move.getType() == ParsedMoveType.FOLD) {
        return node.children.get(move);
      }
    }
    throw new IllegalStateException("Couldn't find a fold node");
  }

  private final ParsedMovesNode findRaiseNode(final NLHand hand, final ParsedMovesNode node,
      final int raiseValue) {
    log.info("Looking for raise node with value {}", raiseValue);
    for (ParsedMove move : node.children.keySet()) {
      if (move.getType() == ParsedMoveType.NUMERIC && move.getNumericValue() == raiseValue) {
        return node.children.get(move);
      }
      if (move.getType() == ParsedMoveType.MAX_BET_MULTIPLIER
          && getMaxBetBet(hand, move.getMultiplier()) == raiseValue) {
        return node.children.get(move);
      }
      if (move.getType() == ParsedMoveType.ALL_IN) {
        final RaiseRange raiseRange = hand.getBetChoice().getRaiseRange();
        if (raiseRange.exists() && raiseRange.getMax() == raiseValue) {
          return node.children.get(move);
        }
      }
    }
    throw new IllegalStateException("Couldn't find a raise node");
  }

  public final void print() {
    print(root, 0);
  }

  private final void print(final ParsedMovesNode node, final int depth) {
    final StringBuilder str = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      str.append('\t');
    }
    for (final ParsedMove move : node.children.keySet()) {
      log.info(str.toString() + move);
      print(node.children.get(move), depth + 1);
    }

  }

  public static void main(String[] args) {
    checkArgument(args.length == 1, "Expected exactly one argument (path of the file to parse)");
    final String path = args[0];
    try {
      final NLFormalBetTreeAbstractor tree = read(path);
      tree.print();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}

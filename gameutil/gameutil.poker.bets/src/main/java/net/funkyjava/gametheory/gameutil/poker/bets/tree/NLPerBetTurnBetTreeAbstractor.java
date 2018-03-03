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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetChoice;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.CallValue;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.ParsedMove.ParsedMoveType;

@Slf4j
public class NLPerBetTurnBetTreeAbstractor implements NLBetTreeAbstractor {

  private final List<List<Set<ParsedMove>>> moves = new ArrayList<>();
  private final int nbRounds;

  private static interface MatchingMovesProcessor {
    public void process(final NLHand hand, final ParsedMove parsedMove, final Move move);
  }

  @Override
  public List<Move> movesForHand(final NLHand hand) {
    final List<Move> res = new ArrayList<>();
    matchMoves(hand, new MatchingMovesProcessor() {

      @Override
      public void process(NLHand hand, ParsedMove parsedMove, Move move) {
        if (!res.contains(move))
          res.add(move);
      }
    });
    return res;
  }

  public Map<Move, ParsedMove> parsedMoves(final NLHand hand) {
    final Map<Move, ParsedMove> res = new HashMap<>();
    matchMoves(hand, new MatchingMovesProcessor() {

      @Override
      public void process(NLHand hand, ParsedMove parsedMove, Move move) {
        res.put(move, parsedMove);
      }
    });
    return res;
  }

  public void matchMoves(final NLHand hand, final MatchingMovesProcessor processor) {
    final int round = hand.getBetRoundIndex();
    if (round > nbRounds - 1) {
      return;
    }
    List<Set<ParsedMove>> roundMoves = moves.get(round);
    final int turnIndex = getTurnIndex(hand);
    if (turnIndex < roundMoves.size()) {
      for (ParsedMove move : roundMoves.get(turnIndex)) {
        final Optional<Move> m = ParsedMove.moveFrom(hand, move);
        if (m.isPresent()) {
          Move validMove = m.get();
          processor.process(hand, move, validMove);
        }
      }
    }

    final BetChoice betChoice = hand.getBetChoice();
    final CallValue callValue = betChoice.getCallValue();
    if (callValue.exists()) {
      final int val = callValue.getValue();
      final Move callMove = Move.getCall(betChoice.getPlayer(), val, callValue.getOldBet());
      processor.process(hand, new ParsedMove(ParsedMoveType.CALL, 0, 0, "Call"), callMove);
    }
    if (!callValue.isCheck()) {
      final Move foldMove = Move.getFold(betChoice.getPlayer());
      processor.process(hand, new ParsedMove(ParsedMoveType.FOLD, 0, 0, "Fold"), foldMove);
    }
  }

  private static int getTurnIndex(final NLHand hand) {
    int nbBetTurn = 0;
    List<Move> moves = new ArrayList<>();
    moves.addAll(hand.getBetMoves(hand.getBetRoundIndex()));
    final boolean hasBlinds = hand.getBetRoundIndex() == 0 && hand.isHasBlinds();
    boolean consideredFirstMoveAfterBlinds = false;
    for (Move move : moves) {
      switch (move.getType()) {
        case NO_ANTE:
        case NO_BLIND:
        case ANTE:
        case SB:
        case BB:
        case FOLD:
        case CALL:
          continue;
        case BET:
        case RAISE:
          nbBetTurn++;
          continue;
      }
    }
    return nbBetTurn;
  }

  public NLPerBetTurnBetTreeAbstractor(final InputStream is) {
    try (final Scanner scanner = new Scanner(is);) {
      int lineNb = 0;
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine();
        if (line.trim().isEmpty()) {
          lineNb++;
          continue;
        }
        final String[] turnStrs = line.split("[\t ]*\\|[\t ]*");
        final int nbTurns = turnStrs.length;
        log.info("Line {} : {} bet turns", lineNb, nbTurns);
        List<Set<ParsedMove>> roundMoves = new ArrayList<>();
        for (int i = 0; i < turnStrs.length; i++) {
          final String[] splitted = turnStrs[i].split("[\t ]*-[\t ]*");
          final int counts = splitted.length;
          log.info("Line {} : bet turn {} : {} moves : {}", lineNb, i, counts, turnStrs[i]);
          Set<ParsedMove> turnMoves = new HashSet<>();
          for (int j = 0; j < counts; j++) {
            turnMoves.add(ParsedMove.parse(splitted[j]));
          }
          if (!turnMoves.isEmpty()) {
            roundMoves.add(turnMoves);
          }
        }
        if (!roundMoves.isEmpty()) {
          moves.add(roundMoves);
        }
        lineNb++;
      }
    }
    nbRounds = moves.size();
  }

  public static NLPerBetTurnBetTreeAbstractor read(final InputStream is) {
    return new NLPerBetTurnBetTreeAbstractor(is);
  }

  public static NLPerBetTurnBetTreeAbstractor read(final Path path)
      throws FileNotFoundException, IOException {
    checkArgument(Files.exists(path), "No file at path " + path);
    try (final FileInputStream fis = new FileInputStream(path.toFile())) {
      return read(fis);
    }
  }

  public static NLPerBetTurnBetTreeAbstractor read(final String pathStr)
      throws FileNotFoundException, IOException {
    final Path path = Paths.get(pathStr);
    return read(path);
  }
}

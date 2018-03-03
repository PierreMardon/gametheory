package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.base.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetChoice;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetRange;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.CallValue;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.RaiseRange;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayerData;

@AllArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class ParsedMove {

  public static class ParsedMovesNode {
    public final LinkedHashMap<ParsedMove, ParsedMovesNode> children = new LinkedHashMap<>();
  }

  public static enum ParsedMoveType {
    FOLD, CALL, MIN_BET_RAISE, ALL_IN, NUMERIC, POT_MULTIPLIER, MAX_BET_MULTIPLIER
  }

  private final ParsedMoveType type;
  private final int numericValue;
  private final double multiplier;
  private final String sourceString;

  public static final ParsedMove parse(String val) {
    val = val.trim();
    final String lower = val.toLowerCase();
    switch (lower) {
      case "c":
      case "call":
      case "check":
      case "limp":
        return new ParsedMove(ParsedMoveType.CALL, 0, 0, val);
      case "mb":
      case "minbet":
      case "mr":
      case "minraise":
      case "min":
        return new ParsedMove(ParsedMoveType.MIN_BET_RAISE, 0, 0, val);
      case "allin":
      case "ai":
      case "push":
      case "p":
      case "shove":
      case "s":
        return new ParsedMove(ParsedMoveType.ALL_IN, 0, 0, val);
      case "fold":
      case "f":
        return new ParsedMove(ParsedMoveType.FOLD, 0, 0, val);
    }
    if (lower.startsWith("x")) {
      final Double multiplier = Double.parseDouble(lower.substring(1, lower.length()));
      checkArgument(multiplier >= 0, "Max bet raise multiplier can not be negative");
      return new ParsedMove(ParsedMoveType.MAX_BET_MULTIPLIER, 0, multiplier, val);
    }
    String postFix = null;
    if (lower.startsWith("px")) {
      postFix = lower.substring(2);
    }
    if (lower.startsWith("potx")) {
      postFix = lower.substring(4);
    }
    if (postFix != null) {
      final Double multiplier = Double.parseDouble(postFix);
      checkArgument(multiplier >= 0, "Pot bet multiplier can not be negative");
      return new ParsedMove(ParsedMoveType.POT_MULTIPLIER, 0, multiplier, val);
    }
    final Integer num = Integer.parseInt(lower);
    checkArgument(num >= 0, "Numeric moves cant be negative");
    return new ParsedMove(ParsedMoveType.NUMERIC, num, 0, val);
  }

  public static final Optional<Move> moveFrom(final NLHand hand, final ParsedMove move) {
    switch (move.getType()) {
      case ALL_IN:
        return allInMoveFrom(hand);
      case CALL:
        return callMoveFrom(hand);
      case FOLD:
        return foldMoveFrom(hand);
      case MAX_BET_MULTIPLIER:
        return maxBetMultiplierMoveFrom(hand, move.getMultiplier());
      case MIN_BET_RAISE:
        return minBetRaiseMoveFrom(hand);
      case NUMERIC:
        return numericMoveFrom(hand, move.getNumericValue());
      case POT_MULTIPLIER:
        return potMultiplierMoveFrom(hand, move.getMultiplier());

    }
    return Optional.absent();
  }

  private static final Optional<Move> potMultiplierMoveFrom(final NLHand hand,
      final double multiplier) {
    final int totPots = hand.getTotalPotsValue();
    final int betValue = (int) (totPots * multiplier);
    final BetChoice betChoice = hand.getBetChoice();
    final RaiseRange raiseRange = betChoice.getRaiseRange();
    final int player = hand.getBettingPlayer();
    if (raiseRange.exists()) {
      return Optional.of(Move.getRaise(player,
          Math.max(Math.min(raiseRange.getMax(), betValue), raiseRange.getMin()),
          raiseRange.getOldBet()));
    }
    final BetRange betRange = betChoice.getBetRange();
    if (betRange.exists()) {
      return Optional.of(
          Move.getBet(player, Math.max(Math.min(betRange.getMax(), betValue), betRange.getMin())));
    }
    return Optional.absent();
  }

  private static final Optional<Move> numericMoveFrom(final NLHand hand, final int numValue) {
    final BetChoice betChoice = hand.getBetChoice();
    final RaiseRange raiseRange = betChoice.getRaiseRange();
    final int player = hand.getBettingPlayer();
    if (raiseRange.exists() && numValue <= raiseRange.getMax() && numValue >= raiseRange.getMin()) {
      return Optional.of(Move.getRaise(player, numValue, raiseRange.getOldBet()));
    }
    final BetRange betRange = betChoice.getBetRange();
    if (betRange.exists() && numValue <= betRange.getMax() && numValue >= betRange.getMin()) {
      return Optional.of(Move.getBet(player, numValue));
    }
    final CallValue callVal = betChoice.getCallValue();
    if (callVal.exists() && callVal.getValue() == numValue) {
      return Optional.of(Move.getCall(player, numValue, callVal.getOldBet()));
    }
    return Optional.absent();
  }

  private static final Optional<Move> allInMoveFrom(final NLHand hand) {
    final BetChoice betChoice = hand.getBetChoice();
    final RaiseRange raiseRange = betChoice.getRaiseRange();
    final int player = hand.getBettingPlayer();
    if (raiseRange.exists()) {
      return Optional.of(Move.getRaise(player, raiseRange.getMax(), raiseRange.getOldBet()));
    }
    final BetRange betRange = betChoice.getBetRange();
    if (betRange.exists()) {
      return Optional.of(Move.getBet(player, betRange.getMax()));
    }
    return callMoveFrom(hand);
  }

  private static final Optional<Move> callMoveFrom(final NLHand hand) {
    final BetChoice betChoice = hand.getBetChoice();
    final int player = hand.getBettingPlayer();
    final CallValue callVal = betChoice.getCallValue();
    if (callVal.exists()) {
      return Optional.of(Move.getCall(player, callVal.getValue(), callVal.getOldBet()));
    }
    return Optional.absent();
  }

  private static final Optional<Move> foldMoveFrom(final NLHand hand) {
    final int player = hand.getBettingPlayer();
    return Optional.of(Move.getFold(player));
  }

  private static final Optional<Move> maxBetMultiplierMoveFrom(final NLHand hand,
      final double multiplier) {
    final List<PlayerData> data = hand.getPlayersData();
    int maxBet = 0;
    for (PlayerData pData : data) {
      maxBet = Math.max(maxBet, pData.getBet());
    }
    final int raiseBet = (int) (maxBet * multiplier);
    final BetChoice betChoice = hand.getBetChoice();
    final RaiseRange raiseRange = betChoice.getRaiseRange();
    final int player = hand.getBettingPlayer();
    if (raiseRange.exists()) {
      return Optional.of(Move.getRaise(player,
          Math.max(Math.min(raiseRange.getMax(), raiseBet), raiseRange.getMin()),
          raiseRange.getOldBet()));
    }
    final BetRange betRange = betChoice.getBetRange();
    if (betRange.exists()) {
      return Optional.of(
          Move.getBet(player, Math.max(Math.min(betRange.getMax(), raiseBet), betRange.getMin())));
    }
    return Optional.absent();
  }

  private static final Optional<Move> minBetRaiseMoveFrom(final NLHand hand) {
    final List<PlayerData> data = hand.getPlayersData();
    int maxBet = 0;
    for (PlayerData pData : data) {
      maxBet = Math.max(maxBet, pData.getBet());
    }
    final BetChoice betChoice = hand.getBetChoice();
    final RaiseRange raiseRange = betChoice.getRaiseRange();
    final int player = hand.getBettingPlayer();
    if (raiseRange.exists()) {
      return Optional.of(Move.getRaise(player, raiseRange.getMin(), raiseRange.getOldBet()));
    }
    final BetRange betRange = betChoice.getBetRange();
    if (betRange.exists()) {
      return Optional.of(Move.getBet(player, betRange.getMin()));
    }
    return Optional.absent();
  }
}

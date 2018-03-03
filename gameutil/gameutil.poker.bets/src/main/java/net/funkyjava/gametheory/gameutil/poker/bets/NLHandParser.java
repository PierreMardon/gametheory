package net.funkyjava.gametheory.gameutil.poker.bets;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Optional;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.BetRoundSpec;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.BlindsAnteSpec;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.NoBetPlayerData;
import net.funkyjava.gametheory.io.ProgramArguments;

@Slf4j
public class NLHandParser {

  private static final String hasBlindsPrefix = "blinds-enable=";
  private static final String bbPrefix = "bb=";
  private static final String sbPrefix = "sb=";
  private static final String hasAntePrefix = "ante-enable=";
  private static final String antePrefix = "ante=";
  private static final String playerBBMarker = "bb";
  private static final String playerSBMarker = "sb";

  private int bbVal = -1;
  private int sbVal = -1;
  private int anteVal = -1;
  private int sbIndex = -1;
  private int bbIndex = -1;
  private boolean hasBlinds = false;
  private boolean hasAnte = false;

  private NLHandParser() {}

  public static NLHand parse(final String handStr, final int nbBetRounds) {
    final String[] splitted = handStr.split("\\|");
    checkArgument(splitted.length == 2,
        "Malformed NLHand string, expected two parts separated by '|' : \"" + handStr + "\"");
    final NLHandParser parser = new NLHandParser();
    parser.parseSettings(splitted[0]);
    final List<NoBetPlayerData> players = parser.parsePlayers(splitted[1]);
    final int nbPlayers = players.size();
    final BlindsAnteSpec blindsSpecs =
        new BlindsAnteSpec(parser.hasAnte, parser.hasBlinds, false, parser.sbVal, parser.bbVal,
            parser.anteVal, Collections.<Integer>emptyList(), parser.sbIndex, parser.bbIndex);
    final int firstPlayerIndex = parser.hasBlinds ? (parser.bbIndex + 1) % nbPlayers : 0;
    final BetRoundSpec betsSpec = new BetRoundSpec(firstPlayerIndex, parser.bbVal);
    return new NLHand(players, blindsSpecs, betsSpec, nbBetRounds);
  }

  private final void parseSettings(final String settingsStr) {
    final String[] args = ProgramArguments.splitArguments(settingsStr);
    final Optional<Boolean> hasBlindsOpt = ProgramArguments.getBoolArgument(args, hasBlindsPrefix);
    if (hasBlindsOpt.isPresent()) {
      hasBlinds = hasBlindsOpt.get();
    } else {
      log.warn("You should specify if you want blinds to be payed with " + hasBlindsPrefix
          + "true|false. By default, they will.");
      hasBlinds = true;
    }

    final Optional<Integer> bbValOpt =
        ProgramArguments.getStrictlyPositiveIntArgument(args, bbPrefix);
    checkArgument(bbValOpt.isPresent(),
        "No bb value specified, add " + bbPrefix + "XX to the hand string");
    bbVal = bbValOpt.get();
    final Optional<Boolean> anteOpt = ProgramArguments.getBoolArgument(args, hasAntePrefix);
    if (anteOpt.isPresent()) {
      hasAnte = anteOpt.get();
    } else {
      hasAnte = false;
      log.info("Ante activation was not specified, by default they will be ignored");
    }
    final Optional<Integer> anteValOpt =
        ProgramArguments.getStrictlyPositiveIntArgument(args, antePrefix);
    if (anteValOpt.isPresent()) {
      if (!hasAnte) {
        log.warn("You specified ante value but ante are not enabled, you can do it with "
            + hasAntePrefix + "=true");
      } else {
        anteVal = anteValOpt.get();
      }
    }
    final Optional<Integer> sbValOpt =
        ProgramArguments.getStrictlyPositiveIntArgument(args, sbPrefix);
    if (sbValOpt.isPresent()) {
      if (!hasBlinds) {
        log.warn("SB value specified, but blinds are disabled, value ignored");
      } else {
        sbVal = sbValOpt.get();
      }
    }
  }

  private final List<NoBetPlayerData> parsePlayers(final String str) {
    final List<NoBetPlayerData> res = new LinkedList<>();
    final String[] playersStr = str.split("-");
    final int length = playersStr.length;
    checkArgument(length >= 2, "Must specify at least two players configurations");
    for (int i = 0; i < length; i++) {
      res.add(parsePlayer(playersStr[i], i));
    }
    return res;
  }

  private final NoBetPlayerData parsePlayer(final String str, final int index) {
    final String[] args = str.trim().split("[ \t]");
    final int length = args.length;
    checkArgument(length > 0 && length < 3, "Wrong format for player at index " + index);
    int stack = -1;
    try {
      stack = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Unable to parse stack for player index " + index, e);
    }
    checkArgument(stack > 0, "Find null or negative stack for player at index " + index);
    if (length > 1) {
      switch (args[1]) {
        case playerBBMarker:
          checkArgument(bbIndex < 0, "Two players declared as BB");
          bbIndex = index;
          break;
        case playerSBMarker:
          checkArgument(sbIndex < 0, "Two players declared as SB");
          sbIndex = index;
          break;
      }
    }
    return new NoBetPlayerData(index, stack, true);
  }

}

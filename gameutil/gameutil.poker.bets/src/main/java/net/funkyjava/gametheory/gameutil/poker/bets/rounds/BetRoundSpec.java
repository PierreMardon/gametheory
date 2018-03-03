package net.funkyjava.gametheory.gameutil.poker.bets.rounds;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BetRoundSpec {

  @Getter
  private final int firstPlayerId;
  @Getter
  private final int bigBlindValue;

}

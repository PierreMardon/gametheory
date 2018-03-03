package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluator;

@Slf4j
public class ThreePlayersEquityRawEvaluatorTest {
  @Test
  public void testTimeOnePreflopHand() {
    IntCardsSpec specs = new DefaultIntCardsSpecs();
    ThreePlayersEquityRawEvaluator eval =
        new ThreePlayersEquityRawEvaluator(specs, new TwoPlusTwoEvaluator());
    String h1 = "Ad";
    String h2 = "Ac";
    String v11 = "Ah";
    String v12 = "Kc";
    String v21 = "6s";
    String v22 = "7s";
    Cards52Strings cString = new Cards52Strings(specs);
    int[] heroCards = {cString.getCard(h1), cString.getCard(h2)};
    int[] vilain1Cards = {cString.getCard(v11), cString.getCard(v12)};
    int[] vilain2Cards = {cString.getCard(v21), cString.getCard(v22)};
    long start = System.currentTimeMillis();
    double[][] res = eval.getValues(heroCards, vilain1Cards, vilain2Cards);
    log.info("Equity for one hand {}{} vs {}{} vs {}{} took : {} ms and its equities are {}", h1,
        h2, v11, v12, v21, v22, System.currentTimeMillis() - start, res);
    String b1 = "4d";
    String b2 = "5d";
    String b3 = "Jh";
    int[] flopCards = {cString.getCard(b1), cString.getCard(b2), cString.getCard(b3)};
    start = System.currentTimeMillis();
    res = eval.getValues(heroCards, vilain1Cards, vilain2Cards, flopCards);
    log.info(
        "Equity for one hand {}{} vs {}{} vs {}{} with flop {}{}{} took : {} ms and its equities are {}",
        h1, h2, v11, v12, v21, v22, b1, b2, b3, System.currentTimeMillis() - start, res);
  }
}

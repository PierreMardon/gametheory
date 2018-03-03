package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluator;

@Slf4j
public class HUEquityRawEvaluatorTest {
  @Test
  public void testTimeOnePreflopHand() {
    IntCardsSpec specs = new DefaultIntCardsSpecs();
    HUEquityRawEvaluator eval = new HUEquityRawEvaluator(specs, new TwoPlusTwoEvaluator());
    String c1 = "Ad";
    String c2 = "Ac";
    String c3 = "Ah";
    String c4 = "Kc";
    Cards52Strings cString = new Cards52Strings(specs);
    int[] heroCards = {cString.getCard(c1), cString.getCard(c2)};
    int[] vilainCards = {cString.getCard(c3), cString.getCard(c4)};
    long start = System.currentTimeMillis();
    double res = eval.getValue(heroCards, vilainCards);
    log.info("Equity for one hand {}{} vs {}{} took : {} ms and its equity is {}", c1, c2, c3, c4,
        System.currentTimeMillis() - start, res);
    String b1 = "4d";
    String b2 = "5d";
    String b3 = "Jh";
    int[] flopCards = {cString.getCard(b1), cString.getCard(b2), cString.getCard(b3)};
    start = System.currentTimeMillis();
    res = eval.getValue(heroCards, vilainCards, flopCards);
    log.info("Equity for one hand {}{} vs {}{} with flop {}{}{} took : {} ms and its equity is {}",
        c1, c2, c3, c4, b1, b2, b3, System.currentTimeMillis() - start, res);
  }
}

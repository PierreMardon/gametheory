package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.BetRoundSpec;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.BlindsAnteSpec;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.BlindsAnteSpec.BlindsAnteSpecBuilder;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.NoBetPlayerData;

/**
 *
 * @author Pierre Mardon
 *
 */
@Slf4j
public class NLHandBetTreeBuilderTest {

  /**
   * Simply test the bet tree generation for an arbitrary situation
   */
  @Test
  public void test() {
    final int stack = 200;
    final int p1 = 0;
    final int p2 = 1;
    final int p3 = 2;
    final NoBetPlayerData p1Data = new NoBetPlayerData(p1, stack, true);
    final NoBetPlayerData p2Data = new NoBetPlayerData(p2, stack, true);
    final NoBetPlayerData p3Data = new NoBetPlayerData(p3, stack, true);
    final List<NoBetPlayerData> pData = new ArrayList<>();
    pData.add(p1Data);
    pData.add(p2Data);
    pData.add(p3Data);

    final BlindsAnteSpecBuilder blindsSpecs = BlindsAnteSpec.builder();
    blindsSpecs.anteValue(0);
    blindsSpecs.bbPlayer(p2);
    blindsSpecs.sbPlayer(p1);
    blindsSpecs.playersHavingToPayEnteringBB(new ArrayList<Integer>());
    blindsSpecs.enableAnte(false);
    blindsSpecs.enableBlinds(true);
    blindsSpecs.sbValue(10);
    blindsSpecs.bbValue(20);
    blindsSpecs.isCash(false);

    final BetRoundSpec betSpecs = new BetRoundSpec(p3, 20);

    final NLHand hand = new NLHand(pData, blindsSpecs.build(), betSpecs, 2);

    final NLBetTreeAbstractor abstractor = new TestAbstractor();
    final NLBetTreePrinter printer = new NLBetTreePrinter();

    final NLAbstractedBetTree treeImperfect = new NLAbstractedBetTree(hand, abstractor, false);
    int[] firstNodesCountsImperfect =
        {treeImperfect.betRoundsFirstNodes[0].length, treeImperfect.betRoundsFirstNodes[1].length};
    int[] betNodesCountsImperfect =
        {treeImperfect.betRoundsNodes[0].length, treeImperfect.betRoundsNodes[1].length};
    log.info(
        "Imperfect recall : \nshowdown nodes {}\nno showdown nodes {}\nround entry nodes {}\nbet nodes{}\n",
        treeImperfect.showdownNodes.length, treeImperfect.noShowdownNodes.length,
        firstNodesCountsImperfect, betNodesCountsImperfect);

    final NLAbstractedBetTree treePerfect = new NLAbstractedBetTree(hand, abstractor, true);
    int[] firstNodesCountsPerfect =
        {treePerfect.betRoundsFirstNodes[0].length, treePerfect.betRoundsFirstNodes[1].length};
    int[] betNodesCountsPerfect =
        {treePerfect.betRoundsNodes[0].length, treePerfect.betRoundsNodes[1].length};
    log.info(
        "Perfect recall : \nshowdown nodes {}\nno showdown nodes {}\nrounds entry nodes {}\nbet nodes{}\n",
        treePerfect.showdownNodes.length, treePerfect.noShowdownNodes.length,
        firstNodesCountsPerfect, betNodesCountsPerfect);

    treeImperfect.walk(printer);
    treePerfect.walk(printer);
  }
}

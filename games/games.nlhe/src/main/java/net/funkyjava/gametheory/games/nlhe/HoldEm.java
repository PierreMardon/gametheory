package net.funkyjava.gametheory.games.nlhe;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.extensiveformgame.ActionTreeNode;
import net.funkyjava.gametheory.extensiveformgame.ActionTreePlayerChoiceTransition;
import net.funkyjava.gametheory.extensiveformgame.ChancesPayouts;
import net.funkyjava.gametheory.extensiveformgame.Game;
import net.funkyjava.gametheory.extensiveformgame.PlayerNode;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHandParser;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.SharedPot;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.NoBetPlayerData;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayerData;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLAbstractedBetTree;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLBetTreeAbstractor;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLBetTreeNode;
import net.funkyjava.gametheory.gameutil.poker.bets.tree.NLBetTreePrinter;

/**
 * No Limit Hold'em extensive form game representation
 * 
 * @author Pierre Mardon
 *
 * @param <Chances> the chances type
 */
@Slf4j
public class HoldEm<Chances> implements Game<NLBetTreeNode, Chances> {

  private final int nbRounds;
  private final int nbPlayers;
  private final NLAbstractedBetTree betTree;
  private final HEEquityProvider<Chances> equityProvider;
  private final int[][] roundChancesSizes;

  /**
   * Constructor
   * 
   * @param betTree the bet tree
   * @param roundChancesSizes size of the chances for each bet round (assumed equal for each player)
   * @param equityProvider the equity provider for showdown
   */
  public HoldEm(final NLAbstractedBetTree betTree, final int[] roundChancesSizes,
      final HEEquityProvider<Chances> equityProvider) {
    this.equityProvider = equityProvider;
    this.betTree = betTree;
    final int nbRounds = this.nbRounds = betTree.nbOfBetRounds;
    log.info("Bet tree rounds : {}", nbRounds);
    betTree.walk(new NLBetTreePrinter());
    checkArgument(this.nbRounds == roundChancesSizes.length,
        "The number of rounds is not consistant between the bet tree (%s) and the round chances sizes (%s)",
        this.nbRounds, roundChancesSizes.length);
    final int nbPlayers = this.nbPlayers = betTree.getNbPlayers();
    final int[][] roundsPlayersChancesSizes = this.roundChancesSizes = new int[nbRounds][nbPlayers];
    for (int i = 0; i < nbRounds; i++) {
      for (int j = 0; j < nbPlayers; j++) {
        roundsPlayersChancesSizes[i][j] = roundChancesSizes[i];
      }
    }
  }

  /**
   * Convenience method to construct from a formal bet tree file and other needed parameters
   * 
   * @param formalBetTreePath the path to the formal bet tree file
   * @param handString the hand string representation to be fed to {@link NLHandParser}
   * @param roundChancesSizes the round chances sizes
   * @param equityProvider the equity provider
   * @param perfectRecall perfect recall boolean. When false, the first nodes of each round may have
   *        multiple parents
   * @return the built NLHE game
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static <Chances> HoldEm<Chances> get(final String formalBetTreePath,
      final String handString, final int[] roundChancesSizes,
      final HEEquityProvider<Chances> equityProvider, final boolean perfectRecall)
      throws FileNotFoundException, IOException {
    final NLHand hand = NLHandParser.parse(handString, roundChancesSizes.length);
    final NLBetTreeAbstractor abstractor = NLBetTreeAbstractor.read(formalBetTreePath);
    final NLAbstractedBetTree betTree = new NLAbstractedBetTree(hand, abstractor, perfectRecall);
    return new HoldEm<>(betTree, roundChancesSizes, equityProvider);
  }

  @Override
  public int[][] roundChancesSizes() {
    return roundChancesSizes;
  }

  @Override
  public int getNbPlayers() {
    return nbPlayers;
  }

  @Override
  public ActionTreeNode<NLBetTreeNode, Chances> rootNode() {
    return getNode(betTree.getRootNode());
  }

  private ActionTreeNode<NLBetTreeNode, Chances> getNode(final NLBetTreeNode node) {
    switch (node.roundState) {
      case END_NO_SHOWDOWN:
        return new ActionTreeNode<>(getPayouts(node));
      case SHOWDOWN:
        return new ActionTreeNode<>(getChancesPayouts(node));
      case WAITING_MOVE:
        return new ActionTreeNode<>(getPlayerNode(node),
            node.isRoundFirstNode && !betTree.isPerfectRecall(), new PlayerTransition(node));
      default:
        throw new IllegalArgumentException();

    }
  }

  private static final double[] getPayouts(final NLBetTreeNode node) {
    final NLHand hand = node.getHand();
    final List<NoBetPlayerData> initialData = hand.getInitialPlayersData();
    final int nbPlayers = hand.getNbPlayers();
    final List<PlayerData> endData = hand.getPlayersData();
    final double[] payouts = new double[nbPlayers];
    for (int i = 0; i < nbPlayers; i++) {
      payouts[i] = endData.get(i).getStack() - initialData.get(i).getStack();
    }
    final List<SharedPot> pots = hand.getSharedPots().get();
    final int winningPlayer = hand.getNoShowdownWinningPlayer();
    int addToWinner = 0;
    for (SharedPot pot : pots) {
      addToWinner += pot.getPot().getValue();
    }
    payouts[winningPlayer] += addToWinner;
    return payouts;
  }

  private final ChancesPayouts<Chances> getChancesPayouts(final NLBetTreeNode node) {
    return new HEChancesPayouts<>(node.getHand(), equityProvider);
  }

  private static final PlayerNode<NLBetTreeNode> getPlayerNode(final NLBetTreeNode node) {
    return new PlayerNode<>(node.playerIndex, node.betRoundIndex, node.nbChildren, node);
  }

  private class PlayerTransition
      implements ActionTreePlayerChoiceTransition<NLBetTreeNode, Chances> {

    private final NLBetTreeNode betNode;

    PlayerTransition(final NLBetTreeNode betNode) {
      this.betNode = betNode;
    }


    @Override
    public ActionTreeNode<NLBetTreeNode, Chances> nodeForAction(int actionIndex) {
      final Move move = betNode.getOrderedMoves().get(actionIndex);
      return getNode(betNode.getChildren().get(move));
    }

  }

}

package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.RoundType;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.anteround.AnteValue;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround.BlindValue;

@Slf4j
public class NLAbstractedBetTree {

  @Getter
  private final NLHand originalHand;
  @Getter
  private final NLBetTreeNode rootNode;
  @Getter
  private final boolean perfectRecall;
  @Getter
  private int maxNbOfActions;
  @Getter
  private final int nbPlayers;

  public final int nbOfBetRounds;
  public final NLBetTreeNode[] showdownNodes;
  public final NLBetTreeNode[] noShowdownNodes;
  public final NLBetTreeNode[][] betRoundsNodes;
  public final NLBetTreeNode[][] betRoundsFirstNodes;

  private List<NLBetTreeNode> showdownNodesList = new ArrayList<>();
  private List<NLBetTreeNode> noShowdownNodesList = new ArrayList<>();
  private List<List<NLBetTreeNode>> betRoundsNodesList = new ArrayList<>();
  private List<List<NLBetTreeNode>> betRoundsFirstNodesList = new ArrayList<>();

  public NLAbstractedBetTree(@NonNull final NLHand hand,
      @NonNull final NLBetTreeAbstractor abstractor, final boolean perfectRecall) {
    this.perfectRecall = perfectRecall;
    originalHand = hand.clone();
    nbOfBetRounds = hand.getNbBetRounds();
    for (int i = 0; i < nbOfBetRounds; i++) {
      betRoundsFirstNodesList.add(new ArrayList<NLBetTreeNode>());
      betRoundsNodesList.add(new ArrayList<NLBetTreeNode>());
    }
    this.nbPlayers = hand.getNbPlayers();
    rootNode = nodeFor(hand, abstractor);
    showdownNodes = toArray(showdownNodesList);
    noShowdownNodes = toArray(noShowdownNodesList);
    betRoundsNodes = toDoubleArray(betRoundsNodesList);
    betRoundsFirstNodes = toDoubleArray(betRoundsFirstNodesList);
    showdownNodesList = null;
    noShowdownNodesList = null;
    betRoundsNodesList = null;
    betRoundsFirstNodesList = null;
  }

  private NLBetTreeNode nodeFor(@NonNull final NLHand hand,
      @NonNull final NLBetTreeAbstractor abstractor) {
    switch (hand.getRoundState()) {
      case CANCELED:
        throw new IllegalStateException("Hand state is CANCELED");
      case SHOWDOWN:
        return findShowdownMatchOrCreate(hand);
      case END_NO_SHOWDOWN:
        return findNoShowdownMatchOrCreate(hand);
      case NEXT_ROUND:
        switch (hand.getRoundType()) {
          case ANTE:
            checkState(hand.nextRoundAfterAnte());
            break;
          case BETS:
            checkState(hand.nextBetRound());
            break;
          case BLINDS:
            checkState(hand.betRoundAfterBlinds());
            break;
        }
        return nodeFor(hand, abstractor);
      case WAITING_MOVE:
        if (hand.getRoundType() == RoundType.ANTE) {
          final Map<Integer, AnteValue> antes = hand.getMissingAnte();
          for (final Integer antePlayer : antes.keySet()) {
            hand.doMove(Move.getAnte(antePlayer, antes.get(antePlayer).getValue()));
          }
          return nodeFor(hand, abstractor);
        }
        if (hand.getRoundType() == RoundType.BLINDS) {
          final Map<Integer, BlindValue> blinds = hand.getMissingBlinds();
          for (final Integer blindsPlayer : blinds.keySet()) {
            final BlindValue blind = blinds.get(blindsPlayer);
            switch (blind.getType()) {
              case BB:
                hand.doMove(Move.getBb(blindsPlayer, blind.getValue()));
                break;
              case SB:
                hand.doMove(Move.getSb(blindsPlayer, blind.getValue()));
                break;
            }
          }
          return nodeFor(hand, abstractor);
        }
        return findBetNodeMatchOrCreate(hand, abstractor);
    }
    return null;
  }

  private NLBetTreeNode findShowdownMatchOrCreate(@NonNull final NLHand hand) {
    final int index = showdownNodesList.size();
    final NLBetTreeNode tmpNode =
        new NLBetTreeNode(hand, new LinkedHashMap<Move, NLBetTreeNode>(), index);
    for (int i = 0; i < index; i++) {
      final NLBetTreeNode node = showdownNodesList.get(i);
      if (node.equalsForShowdown(tmpNode)) {
        return node;
      }
    }
    showdownNodesList.add(tmpNode);
    return tmpNode;
  }

  private NLBetTreeNode findNoShowdownMatchOrCreate(@NonNull final NLHand hand) {
    final int index = noShowdownNodesList.size();
    final NLBetTreeNode tmpNode =
        new NLBetTreeNode(hand, new LinkedHashMap<Move, NLBetTreeNode>(), index);
    for (int i = 0; i < index; i++) {
      final NLBetTreeNode node = noShowdownNodesList.get(i);
      if (node.equalsForNoShowdown(tmpNode)) {
        return node;
      }
    }
    noShowdownNodesList.add(tmpNode);
    return tmpNode;
  }

  private NLBetTreeNode findBetNodeMatchOrCreate(@NonNull final NLHand hand,
      final NLBetTreeAbstractor abstractor) {

    final List<Move> moves = hand.getBetMoves(hand.getBetRoundIndex());
    boolean startingNode = false;
    if (moves.isEmpty()) {
      startingNode = true;
      if (!perfectRecall) {
        final List<NLBetTreeNode> startingNodes =
            betRoundsFirstNodesList.get(hand.getBetRoundIndex());
        for (final NLBetTreeNode node : startingNodes) {
          if (node.samePlayersData(hand)) {
            return node;
          }
        }
      }
    }
    final List<Move> nextMoves = abstractor.movesForHand(hand);
    checkArgument(!nextMoves.isEmpty(),
        "Bet tree abstractor returned no move, missing move after \n- ante moves : %s\n- blinds moves : %s\n- bet moves : %s",
        hand.getAnteMoves(), hand.getBlindsMoves(), hand.getBetMoves());
    maxNbOfActions = Math.max(maxNbOfActions, nextMoves.size());
    // We use a linked hash map to keep the insertion order on the keys
    final LinkedHashMap<Move, NLBetTreeNode> children = new LinkedHashMap<>();
    for (Move move : nextMoves) {
      final NLHand newHand = hand.clone();
      checkState(newHand.doMove(move),
          "Move %s seems invalid after \n- ante moves : %s\n- blinds moves : %s\n- bet moves : %s",
          move, hand.getAnteMoves(), hand.getBlindsMoves(), hand.getBetMoves());
      checkState(!children.containsKey(move),
          "The same move %s was provided twice after \n- ante moves : %s\n- blinds moves : %s\n- bet moves : %s",
          move, hand.getAnteMoves(), hand.getBlindsMoves(), hand.getBetMoves());
      children.put(move, nodeFor(newHand, abstractor));
    }
    final int betRound = hand.getBetRoundIndex();
    final int index = betRoundsNodesList.get(betRound).size();
    final NLBetTreeNode node = new NLBetTreeNode(hand, children, index);
    betRoundsNodesList.get(betRound).add(node);
    if (startingNode) {
      betRoundsFirstNodesList.get(betRound).add(node);
    }
    return node;
  }

  private static NLBetTreeNode[] toArray(List<NLBetTreeNode> list) {
    return list.toArray(new NLBetTreeNode[list.size()]);
  }

  @SuppressWarnings("unchecked")
  private static NLBetTreeNode[][] toDoubleArray(List<List<NLBetTreeNode>> list) {
    final NLBetTreeNode[][] res = new NLBetTreeNode[list.size()][];
    int i = 0;
    for (List<NLBetTreeNode> subList : list) {
      res[i++] = toArray(subList);
    }
    return res;
  }

  public void walk(final NLBetTreeWalker walker) {
    walkRec(walker, rootNode, new ArrayList<NLBetTreeNode>(), null);
  }

  private static void walkRec(final NLBetTreeWalker walker, final NLBetTreeNode node,
      final List<NLBetTreeNode> parents, final Move lastMove) {
    walker.handleCurrentNode(node, parents, Optional.fromNullable(lastMove));
    parents.add(node);
    for (final Move move : node.getChildren().keySet()) {
      walkRec(walker, node.getChildren().get(move), parents, move);
    }
    parents.remove(node);
  }
}

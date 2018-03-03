/**
 *
 */
package net.funkyjava.gametheory.gameutil.poker.bets.pots;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * A shared pot is a pot divided between winners
 *
 * @author Pierre Mardon
 *
 */
@ToString
public class SharedPot {

  @Getter
  private final List<PotShare> shares;
  @Getter
  private final Pot pot;

  private SharedPot(Pot pot, List<PotShare> shares) {
    this.pot = pot;
    this.shares = Collections.unmodifiableList(shares);
  }

  /**
   * Create a shared pot
   *
   * @param pot the source pot
   * @param winners the list of winning players
   * @param oddChipsWinner the odd chips winner
   * @return the resulting shared pot
   */
  public static SharedPot sharePot(@NonNull Pot pot, @NonNull List<Integer> winners,
      @NonNull Integer oddChipsWinner) {
    checkArgument(winners.contains(oddChipsWinner),
        "List of winners must contain the odd chips winner");
    checkArgument(pot.getPlayers().containsAll(winners),
        "The winners didn't all contribute to the pot");
    List<PotShare> shares = new LinkedList<>();
    final int val = pot.getValue();
    int share = val / winners.size();
    int extra = val % winners.size();
    for (Integer winner : winners) {
      if (winner != oddChipsWinner) {
        shares.add(new PotShare(share, winner));
      }
    }
    shares.add(new PotShare(share + extra, oddChipsWinner));
    return new SharedPot(pot, shares);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SharedPot) {
      final SharedPot sharedPot = (SharedPot) obj;
      if (shares.size() != sharedPot.shares.size()) {
        return false;
      }
      for (final PotShare share : sharedPot.shares) {
        if (!shares.contains(share)) {
          return false;
        }
      }
      return true;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return shares.size();
  }
}

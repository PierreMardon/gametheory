package net.funkyjava.gametheory.cscfrm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Chances synchronizer with no additional thread required. Each consumer produces the requested
 * chances.
 * 
 * @author Pierre Mardon
 *
 * @param <Chances>
 */
public class CSCFRMMutexChancesSynchronizer<Chances extends CSCFRMChances>
    implements CSCFRMChancesSynchronizer<Chances> {

  private final CSCFRMChancesProducer<Chances> producer;
  private final int nbRounds;
  private final BitSet[][] inUseBits;
  private final List<Chances> collisionChances = new ArrayList<>();
  private final List<Chances> availableChances = new LinkedList<>();
  private boolean stop = false;

  /**
   * Constructor
   * 
   * @param producer the chances producer whose access will be synchronized
   * @param chancesSizes the size of the chances for each round and player
   */
  public CSCFRMMutexChancesSynchronizer(final CSCFRMChancesProducer<Chances> producer,
      final int[][] chancesSizes) {
    this.producer = producer;
    final int nbRounds = this.nbRounds = chancesSizes.length;
    this.inUseBits = new BitSet[nbRounds][];
    for (int i = 0; i < nbRounds; i++) {
      final int[] playersChances = chancesSizes[i];
      final int nbPlayers = playersChances.length;
      final BitSet[] roundBits = inUseBits[i] = new BitSet[nbPlayers];
      for (int j = 0; j < nbPlayers; j++) {
        roundBits[j] = new BitSet(playersChances[j]);
      }
    }
  }

  @Override
  public Chances getChances() throws InterruptedException {
    final List<Chances> availableChances = this.availableChances;
    final CSCFRMChancesProducer<Chances> producer = this.producer;
    synchronized (this) {
      if (stop) {
        return null;
      }
      if (!availableChances.isEmpty()) {
        return availableChances.remove(0);
      }
      final List<Chances> collisionChances = this.collisionChances;
      while (true) {
        final Chances chances = producer.produceChances();
        final int[][] playersChances = chances.getPlayersChances();
        if (hasCollision(playersChances)) {
          collisionChances.add(chances);
          continue;
        }
        reserve(playersChances);
        return chances;
      }
    }
  }

  @Override
  public void endUsing(final Chances used) throws InterruptedException {
    final int[][] playersChances = used.getPlayersChances();
    final CSCFRMChancesProducer<Chances> producer = this.producer;
    synchronized (this) {

      endReserving(playersChances);
      producer.endedUsing(used);
      if (stop) {
        return;
      }
      final List<Chances> collisionChances = this.collisionChances;
      final List<Chances> availableChances = this.availableChances;
      int nbCollision = collisionChances.size();
      for (int i = 0; i < nbCollision;) {
        final Chances chances = collisionChances.get(i);
        final int[][] pChances = chances.getPlayersChances();
        if (!hasCollision(pChances)) {
          availableChances.add(chances);
          reserve(pChances);
          collisionChances.remove(i);
          nbCollision--;
        } else {
          i++;
        }
      }
    }
  }

  private final void reserve(final int[][] usedChances) {
    final int nbRounds = this.nbRounds;
    final BitSet[][] inUseBits = this.inUseBits;
    for (int i = 0; i < nbRounds; i++) {
      final BitSet[] roundBits = inUseBits[i];
      final int[] roundChances = usedChances[i];
      final int nbPlayers = roundChances.length;
      for (int j = 0; j < nbPlayers; j++) {
        roundBits[j].set(roundChances[j]);
      }
    }
  }

  private final void endReserving(final int[][] usedChances) {
    final int nbRounds = this.nbRounds;
    final BitSet[][] inUseBits = this.inUseBits;
    for (int i = 0; i < nbRounds; i++) {
      final BitSet[] roundBits = inUseBits[i];
      final int[] roundChances = usedChances[i];
      final int nbPlayers = roundChances.length;
      for (int j = 0; j < nbPlayers; j++) {
        roundBits[j].clear(roundChances[j]);
      }
    }
  }

  private final boolean hasCollision(final int[][] chances) {
    final int nbRounds = this.nbRounds;
    final BitSet[][] inUseBits = this.inUseBits;
    for (int i = 0; i < nbRounds; i++) {
      final BitSet[] roundBits = inUseBits[i];
      final int[] roundChances = chances[i];
      final int nbPlayers = roundChances.length;
      for (int j = 0; j < nbPlayers; j++) {
        if (roundBits[j].get(roundChances[j])) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void stop() {
    final List<Chances> collisionChances = this.collisionChances;
    final List<Chances> availableChances = this.availableChances;
    final CSCFRMChancesProducer<Chances> producer = this.producer;
    synchronized (this) {
      this.stop = true;
      for (Chances chances : collisionChances) {
        producer.endedUsing(chances);
      }
      collisionChances.clear();
      for (Chances chances : availableChances) {
        endReserving(chances.getPlayersChances());
        producer.endedUsing(chances);
      }
      availableChances.clear();
    }
  }

  @Override
  public void reset() {
    synchronized (this) {
      stop = false;
    }
  }

  @Override
  public List<Runnable> getProducers() {
    return Collections.emptyList();
  }

}

package net.funkyjava.gametheory.cscfrm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.NonNull;

/**
 * Runs the CSCFRM algorithm using {@link CSCFRMTrainer} executed in a {@link Executor}
 * 
 * @author Pierre Mardon
 *
 * @param <Chances> the chances class
 */
public class CSCFRMRunner<Chances extends CSCFRMChances> {

  @Getter
  private final CSCFRMData<?, Chances> data;
  private final int nbTrainerThreads;

  private ExecutorService executor = null;
  private final CSCFRMChancesSynchronizer<Chances> chancesSynchronizer;
  private boolean stop = false;
  private Runnable[] trainerRunnables;
  private final List<Exception> exceptions =
      Collections.synchronizedList(new LinkedList<Exception>());
  private final List<CSCFRMHook> hooks = new LinkedList<>();
  private final AtomicInteger hookLockCount = new AtomicInteger();

  private final class TrainerRunnable implements Runnable {

    private final CSCFRMTrainer<Chances> trainer = new CSCFRMTrainer<>(data);

    @Override
    public void run() {
      final CSCFRMTrainer<Chances> trainer = this.trainer;
      final CSCFRMChancesSynchronizer<Chances> chancesSynchronizer =
          CSCFRMRunner.this.chancesSynchronizer;
      try {
        while (!stop) {
          final Chances chances = chancesSynchronizer.getChances();
          if (chances == null) {
            return;
          }
          trainer.train(chances);
          chancesSynchronizer.endUsing(chances);
          lockAndPerformHooks();
        }
      } catch (

      Exception e) {
        e.printStackTrace();
        exceptions.add(e);
      }
    }

  }

  private final void lockAndPerformHooks() throws InterruptedException {
    synchronized (hooks) {
      if (hooks.isEmpty()) {
        return;
      }
      if (hookLockCount.incrementAndGet() == nbTrainerThreads) {
        final List<CSCFRMHook> toRemove = new LinkedList<>();
        for (CSCFRMHook hook : hooks) {
          hook.action();
          if (hook.isOneTime()) {
            toRemove.add(hook);
          }
        }
        for (CSCFRMHook hook : toRemove) {
          hooks.remove(hook);
        }
        hookLockCount.set(0);
        hooks.notifyAll();
      } else {
        hooks.wait();
        hooks.notifyAll();
      }
    }
  }

  /**
   * Constructor
   * 
   * @param data the data containing the game action tree and CSCFRM nodes
   * @param chancesSynchronizer the chances synchronizer that avoids collisions
   * @param nbTrainerThreads the number of threads to use for training
   */
  public CSCFRMRunner(@NonNull final CSCFRMData<?, Chances> data,
      @NonNull final CSCFRMChancesSynchronizer<Chances> chancesSynchronizer,
      final int nbTrainerThreads) {
    checkArgument(nbTrainerThreads > 0, "The number of trainer threads must be > 0");
    this.data = data;
    this.nbTrainerThreads = nbTrainerThreads;
    this.chancesSynchronizer = chancesSynchronizer;
    final Runnable[] trainerRunnables = this.trainerRunnables = new Runnable[nbTrainerThreads];
    for (int i = 0; i < nbTrainerThreads; i++) {
      trainerRunnables[i] = new TrainerRunnable();
    }
  }

  /**
   * Non blocking start
   */
  public synchronized final void start() {
    checkState(executor == null, "An executor is already running");
    exceptions.clear();
    this.stop = false;
    final int nbTrainerThreads = this.nbTrainerThreads;
    final Runnable[] trainerRunnables = this.trainerRunnables;
    chancesSynchronizer.reset();
    final ExecutorService executor =
        this.executor = Executors.newFixedThreadPool(nbTrainerThreads + 1);
    for (Runnable producer : chancesSynchronizer.getProducers()) {
      executor.execute(producer);
    }
    for (int i = 0; i < nbTrainerThreads; i++) {
      executor.execute(trainerRunnables[i]);
    }
  }

  /**
   * Blocking stop
   * 
   * @return a list that would contain all trainer thread exceptions if any
   * @throws InterruptedException
   */
  public synchronized final List<Exception> stopAndAwaitTermination() throws InterruptedException {
    stop();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    executor = null;
    return exceptions;
  }

  /**
   * Non-blocking stop
   */
  public synchronized final void stop() {
    checkState(executor != null, "No executor is running");
    stop = true;
    chancesSynchronizer.stop();
    executor.shutdown();
  }

  /**
   * Is the CSCFRM running
   * 
   * @return true when the CSCFRM is running
   */
  public boolean isRunning() {
    return executor != null;
  }

  public void addHook(final CSCFRMHook hook) {
    synchronized (hooks) {
      hooks.add(hook);
    }
  }

  public void removeHook(final CSCFRMHook hook) {
    synchronized (hooks) {
      hooks.remove(hook);
    }
  }
}

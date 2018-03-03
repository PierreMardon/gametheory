package net.funkyjava.gametheory.gameutil.clustering.neuralnet.convergence;

public interface NetworkConvergenceMonitor {

  boolean shouldStop(int tasksExecuted, int taskSamplesSize);

}

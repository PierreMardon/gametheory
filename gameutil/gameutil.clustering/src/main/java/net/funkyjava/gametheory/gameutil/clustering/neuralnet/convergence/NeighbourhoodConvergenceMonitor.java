package net.funkyjava.gametheory.gameutil.clustering.neuralnet.convergence;

import org.apache.commons.math3.ml.neuralnet.sofm.NeighbourhoodSizeFunction;

public class NeighbourhoodConvergenceMonitor implements NetworkConvergenceMonitor {

  private final NeighbourhoodSizeFunction neighbourhoodSize;

  public NeighbourhoodConvergenceMonitor(final NeighbourhoodSizeFunction neighbourhoodSize) {
    this.neighbourhoodSize = neighbourhoodSize;
  }

  @Override
  public boolean shouldStop(int tasksExecuted, int taskSamplesSize) {
    final long numCalls = (long) tasksExecuted * (long) taskSamplesSize;
    return neighbourhoodSize.value(numCalls) <= 0;
  }

}

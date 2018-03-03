package net.funkyjava.gametheory.gameutil.clustering.neuralnet.convergence;

import java.util.List;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.neuralnet.Network;
import org.apache.commons.math3.ml.neuralnet.sofm.NeighbourhoodSizeFunction;

public class NeighbourhoodConvergenceMonitorProvider implements NetworkConvergenceMonitorProvider {

  private final NeighbourhoodSizeFunction neighbourhoodSize;

  public NeighbourhoodConvergenceMonitorProvider(
      final NeighbourhoodSizeFunction neighbourhoodSize) {
    this.neighbourhoodSize = neighbourhoodSize;
  }

  @Override
  public <T extends Clusterable> NeighbourhoodConvergenceMonitor createMonitor(Network network,
      List<T> points) {
    return new NeighbourhoodConvergenceMonitor(neighbourhoodSize);
  }

}

package net.funkyjava.gametheory.gameutil.clustering.neuralnet.convergence;

import java.util.List;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.neuralnet.Network;

public interface NetworkConvergenceMonitorProvider {
  <T extends Clusterable> NetworkConvergenceMonitor createMonitor(Network network, List<T> points);
}

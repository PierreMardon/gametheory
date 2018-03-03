package net.funkyjava.gametheory.gameutil.clustering.neuralnet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.neuralnet.MapUtils;
import org.apache.commons.math3.ml.neuralnet.Network;
import org.apache.commons.math3.ml.neuralnet.Neuron;
import org.apache.commons.math3.ml.neuralnet.UpdateAction;
import org.apache.commons.math3.ml.neuralnet.sofm.KohonenUpdateAction;
import org.apache.commons.math3.ml.neuralnet.sofm.LearningFactorFunction;
import org.apache.commons.math3.ml.neuralnet.sofm.NeighbourhoodSizeFunction;
import org.apache.commons.math3.random.RandomGenerator;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.clustering.neuralnet.convergence.NetworkConvergenceMonitor;
import net.funkyjava.gametheory.gameutil.clustering.neuralnet.convergence.NetworkConvergenceMonitorProvider;

@Slf4j
public class KohonenClusterer<T extends Clusterable> extends Clusterer<T> {

  private final int taskSamplesSize;
  private final int maxTasks;
  private final KohonenUpdateAction updateAction;
  private final RandomGenerator random;
  private final NetworkProvider networkProvider;
  private final NetworkConvergenceMonitorProvider convergenceMonitorProvider;

  public KohonenClusterer(DistanceMeasure distance, LearningFactorFunction learningFactor,
      NeighbourhoodSizeFunction neighbourhoodSize, NetworkProvider networkProvider,
      int taskSamplesSize, int maxTasks, RandomGenerator random,
      NetworkConvergenceMonitorProvider convergenceMonitorProvider) {
    super(checkNotNull(distance));
    updateAction = new KohonenUpdateAction(distance, checkNotNull(learningFactor),
        checkNotNull(neighbourhoodSize));
    this.networkProvider = checkNotNull(networkProvider);
    checkArgument(taskSamplesSize > 0);
    this.taskSamplesSize = taskSamplesSize;
    this.maxTasks = maxTasks;
    this.random = checkNotNull(random);
    this.convergenceMonitorProvider = convergenceMonitorProvider;
  }

  @Override
  public List<CentroidCluster<T>> cluster(Collection<T> points)
      throws MathIllegalArgumentException, ConvergenceException {
    final int taskSamplesSize = this.taskSamplesSize;
    final int maxTasks = this.maxTasks;
    final Network network = networkProvider.createNetwork();
    final boolean checkConvergence = convergenceMonitorProvider != null;
    final List<T> pointList = Collections.unmodifiableList(new ArrayList<>(points));
    final int pointsSize = pointList.size();
    final NetworkConvergenceMonitor convergenceMonitor = checkConvergence
        ? checkNotNull(convergenceMonitorProvider.createMonitor(network, pointList)) : null;
    final RandomGenerator random = this.random;
    final UpdateAction updateAction = this.updateAction;
    try {
      for (int task = 0; task < maxTasks; task++) {
        for (int feature = 0; feature < taskSamplesSize; feature++) {
          updateAction.update(network, pointList.get(random.nextInt(pointsSize)).getPoint());
        }
        if (checkConvergence && convergenceMonitor.shouldStop(task + 1, taskSamplesSize)) {
          break;
        }
      }
    } catch (Exception e) {
      log.error("Convergence error, the neighbourhood function may return 0", e);
      throw new ConvergenceException();
    }
    final Map<Long, CentroidCluster<T>> clusters = new HashMap<>();
    final DistanceMeasure distance = getDistanceMeasure();
    for (int i = 0; i < pointsSize; i++) {
      final T point = pointList.get(i);
      final Neuron neuron = MapUtils.findBest(point.getPoint(), network, distance);
      final Long identifier = neuron.getIdentifier();
      CentroidCluster<T> cluster = clusters.get(identifier);
      if (cluster == null) {
        clusters.put(identifier,
            cluster = new CentroidCluster<>(new DoublePoint(neuron.getFeatures())));
      }
      cluster.addPoint(point);
    }
    return new ArrayList<>(clusters.values());
  }

}

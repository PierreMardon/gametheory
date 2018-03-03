package net.funkyjava.gametheory.gameutil.clustering.neuralnet;

import org.apache.commons.math3.ml.neuralnet.FeatureInitializer;
import org.apache.commons.math3.ml.neuralnet.Network;
import org.apache.commons.math3.ml.neuralnet.SquareNeighbourhood;
import org.apache.commons.math3.ml.neuralnet.twod.NeuronSquareMesh2D;

public class NeuronSquareMesh2DNetworkProvider implements NetworkProvider {

  private final int numRows;
  private final boolean wrapRowDim;
  private final int numCols;
  private final boolean wrapColDim;
  private final SquareNeighbourhood neighbourhoodType;
  private final FeatureInitializer[] featureInit;

  public NeuronSquareMesh2DNetworkProvider(int numRows, boolean wrapRowDim, int numCols,
      boolean wrapColDim, SquareNeighbourhood neighbourhoodType, FeatureInitializer[] featureInit) {
    this.numRows = numRows;
    this.wrapRowDim = wrapRowDim;
    this.numCols = numCols;
    this.wrapColDim = wrapColDim;
    this.neighbourhoodType = neighbourhoodType;
    this.featureInit = featureInit;
  }

  @Override
  public Network createNetwork() {
    return new NeuronSquareMesh2D(numRows, wrapRowDim, numCols, wrapColDim, neighbourhoodType,
        featureInit).getNetwork();
  }

}

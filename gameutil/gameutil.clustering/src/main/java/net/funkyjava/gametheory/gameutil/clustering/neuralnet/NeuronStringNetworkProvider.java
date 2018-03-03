package net.funkyjava.gametheory.gameutil.clustering.neuralnet;

import org.apache.commons.math3.ml.neuralnet.FeatureInitializer;
import org.apache.commons.math3.ml.neuralnet.Network;
import org.apache.commons.math3.ml.neuralnet.oned.NeuronString;

public class NeuronStringNetworkProvider implements NetworkProvider {

  private final int num;
  private final boolean wrap;
  private final FeatureInitializer[] featureInit;

  public NeuronStringNetworkProvider(int num, boolean wrap, FeatureInitializer[] featureInit) {
    this.num = num;
    this.wrap = wrap;
    this.featureInit = featureInit;
  }

  @Override
  public Network createNetwork() {
    return new NeuronString(num, wrap, featureInit).getNetwork();
  }

}

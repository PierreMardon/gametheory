package net.funkyjava.gametheory.gameutil.clustering.neuralnet;

import org.apache.commons.math3.ml.neuralnet.Network;

public interface NetworkProvider {

  Network createNetwork();

}

package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.input.TeeInputStream;

import net.funkyjava.gametheory.gameutil.poker.bets.NLHand;
import net.funkyjava.gametheory.gameutil.poker.bets.moves.Move;


public interface NLBetTreeAbstractor {

  List<Move> movesForHand(NLHand hand);

  public static NLBetTreeAbstractor read(final InputStream is) throws IOException {
    Exception formalException;
    PipedOutputStream pos = new PipedOutputStream();
    try (PipedInputStream pis = new PipedInputStream(pos);
        TeeInputStream tis = new TeeInputStream(is, pos, true);) {
      try {
        return NLFormalBetTreeAbstractor.read(tis);
      } catch (Exception e) {
        formalException = e;
      }
      Exception betTurnException;

      try {
        return NLPerBetTurnBetTreeAbstractor.read(pis);
      } catch (Exception e) {
        betTurnException = e;
      }
      System.err.println("Unable to parse as a formal bet tree");
      formalException.printStackTrace();
      System.err.println("Unable to parse as a bet turns bet tree");
      betTurnException.printStackTrace();
      throw new IllegalArgumentException();
    }
  }

  public static NLBetTreeAbstractor read(final Path path)
      throws FileNotFoundException, IOException {
    checkArgument(Files.exists(path), "No file at path " + path);
    try (final FileInputStream fis = new FileInputStream(path.toFile())) {
      return read(fis);
    }
  }

  public static NLBetTreeAbstractor read(final String pathStr)
      throws FileNotFoundException, IOException {
    final Path path = Paths.get(pathStr);
    return read(path);
  }
}

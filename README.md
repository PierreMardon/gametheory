# net.funkyJava.gametheory

## Technical prerequisites

You MUST use Lombok ( https://projectlombok.org ) to properly open the project in your IDE (I use Eclipse but it's easy in other IDEs too).

And you MUST learn how to use Maven if you don't already know :)

## Artifacts

### extensiveformgame

Poker-oriented representation of extensive form games (see `Game` interface) : chances and actions are separated, however the round concept is flexible enough to allow many other games to be represented. 

The principle is that a game must be able to provide recursively all action states specifications, so that processing algorithm classes can build their best-fitted representation of the game, especially building the preprocessed action tree using `ActionTree` class and all per round, player and chances nodes using `ActionChancesData` class.

### CSCFRM

Chances-sampling counter-factual regret minimization algorithm.

To use the `CSCFRMRunner` class, you have to provide it :
- A `CSCFRMData` that you can build from any `net.funkyjava.gametheory.extensiveformgame.Game` that has its generic type Chances compatible with the chances synchronizer.
- A `CSCFRMChancesSynchronizer` : the `CSCFRMMutexChancesSynchronizer` is an existing implementation 

`CSCFRMData` will build its fully computed and indexed representation of the game using the `ActionTree` and `ActionChancesData` classes of the `extensiveformgame` artifact.

### games

All games implementations and the high-level tools built with the core artifacts.

#### games.nlhe

NLHE implementations : there's a generic `NoLimitHoldem` class that should allow you to build your implementation using the framework's model, as well as preflop implementations in which you can input any action tree.

#### games.nlhe.javafx

*Work in progress but already working* - A small JavaFX software for three players preflop push/fold nash computation. There is one data file that is missing (for three players preflop reduced equity), because it's too big for Github.

### gameutil

Aims to group together all tools to build your games.

#### gameutil.cards

Cards, deck and indexer representation.

#### gameutil.clustering

Clustering tools using apache-math3 interfaces :
- Experiment for neural network clustering (not useful in practice)
- Generic multithread clusterer (to use with k-means for example)
- Some useful classes to use especially in poker context, see `gameutil.poker.he.clustering` artifact

#### gameutil.poker.bets

- Representation of poker bets rules (for now only NL), see `NLHand` class as an entry point
- Representation of bet tree abstractors to build a bet tree reduced to only some of the possible actions
- Implementation of bet trees :
	- `NLPushFoldBetTreeAbstractor` simple push / fold
	- `NLFormalBetTreeAbstractor` to parse a bet tree specified from an input stream (typically a file).

Accepted actions are case insensitive :

- numeric representation, e.g. `40` for a 40 chips bet, call or raise
- maximum bet multiplier, e.g. `x2.5` for a raise of 2.5 multiplied by the maximum bet of the round
- pot multiplier, starting with either `pX` or `potX`, e.g. `pX3.4` for a bet, call or raise of 3.4 multiplied by the size of the previous rounds pots.
- min bet or raise (bet or raise nature is not checked), `mb`, `minBet`, `mr`, `minRaise` or `min`
- call or check (call or check nature is not checked) `c`, `call`, `check`.
- all in (push or call nature is not checked) `allIn`, `ai`, `push`, `p`, `shove` or `p` 

To ease the parsing, each action except the first one must be preceded with a dash : `-`.

You can avoid repeating first actions of the previous line if they are the same.

Example :
<pre>
AllIn	-Call
	-Fold
x2	-AllIn	-Call
	-	-Fold
	-Call
	-Fold
Fold
</pre>

#### gameutil.poker.he.clustering

- `HoldemOpponentClusterHandStrength` : class to build histograms of hand strength versus opponent clustered hole cards (OCHS) as [shown here](http://poker.cs.ualberta.ca/publications/AAMAS13-abstraction.pdf)
- `HoldemHSClusterer` : class to cluster hands given their HS, EHS or EHS2

These classes use the `AllHoldemHSTables` and `HoldemHSHistograms` classes as well as `WaughIndexer` (see other artifacts).

#### gameutil.poker.he.evaluators

Equity and hand strength classes that should allow you to produce binary files to never compute this again.

#### gameutil.poker.he.handeval

Interfaces for poker hands evaluators.

#### gameutil.poker.he.twoplustwo

Legendary 2+2 cards evaluator.

#### gameutil.poker.he.indexing.djhemlig

Legacy Djhemlig's LUT implementation.

#### gameutil.poker.he.indexing.waugh

Kevin Waugh's perfect indexer implementation ([paper here](https://www.aaai.org/ocs/index.php/WS/AAAIW13/paper/download/7042/6491)), look at the class javadoc for other links.

### io

Just some utility classes to read/write files and command line arguments.


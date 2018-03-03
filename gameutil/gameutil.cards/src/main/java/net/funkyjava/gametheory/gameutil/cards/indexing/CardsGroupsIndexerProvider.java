package net.funkyjava.gametheory.gameutil.cards.indexing;

public interface CardsGroupsIndexerProvider<T extends CardsGroupsIndexer> {
  T getIndexer();
}

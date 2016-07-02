package dfh.anagrams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dfh.thread.ThreadPuddle;

/**
 * A {@link TrieWalker} searches the trie with multiple threads. It holds the
 * work queue and dynamic programming cache this process needs. This cache is
 * held for the life of the walker. If you reuse a walker on a new phrase, the
 * cache from the first set of anagrams may accelerate further collection. On
 * the other hand, it may just waste memory, in which case you are better off
 * replacing the walker.
 * 
 * @author houghton
 *
 */
public class TrieWalker {
	private Trie trie;
	private Map<CharCount, List<PartialEvaluation>> partials = new HashMap<>();
	private Queue<CharCount> work = new ConcurrentLinkedQueue<>();
	private ThreadPuddle pool;
	public Runnable beforeWalk = () -> {
	}, beforeCollect = () -> {
	}, afterCollect = () -> {
	};
	private AnagramStower stower;
	private int threads;

	public TrieWalker(Trie trie, AnagramStower stower) {
		this(trie, stower, Runtime.getRuntime().availableProcessors() + 1);
	}

	public TrieWalker(Trie trie, AnagramStower stower, int threads) {
		this.trie = trie;
		this.stower = stower;
		this.threads = threads;
	}

	/**
	 * @return the number of {@link PartialEvaluation} objects held in the
	 *         dynamic programming cache; this calculation is not synchronized,
	 *         so it could be inaccurate
	 */
	public int size() {
		int size = 0;
		for (List<?> l : partials.values()) {
			size += l.size();
		}
		return size;
	}

	public int numberCharacterCounts() {
		return partials.size();
	}

	public int numberPartialEvaluations() {
		int n = 0;
		for (List<?> l : partials.values()) {
			n += l.size();
		}
		return n;
	}

	public void anagrams(String phrase, Runnable stowerAction) {
		CharCount baseCount = trie.characterCount(phrase);
		if (baseCount == null) {
			return;
		}
		synchronized (partials) {
			if (!partials.containsKey(baseCount)) {
				work.add(baseCount);
			}
		}
		walk(baseCount.total);
		collect(baseCount, stowerAction);
	}

	class WordBucket {
		int size = -1;
		WordBucket parent;
		PartialEvaluation pe;

		public WordBucket(PartialEvaluation pe) {
			this.pe = pe;
		}

		int size() {
			if (size == -1) {
				if (parent == null) {
					size = 1;
				} else {
					size = 1 + parent.size();
				}
			}
			return size;
		}

		List<String> dump() {
			List<String> words = new ArrayList<>(size());
			WordBucket n = this;
			while (n != null) {
				words.add(n.pe.translate(trie));
				n = n.parent;
			}
			words.sort(null);
			return words;
		}

		WordBucket fill(PartialEvaluation pe) {
			WordBucket wb = new WordBucket(pe);
			wb.parent = this;
			return wb;
		}
	}

	private void collect(CharCount baseCount, Runnable stowerAction) {
		beforeCollect.run();
		Queue<WordBucket> collectionQueue = new LinkedList<>();
		for (PartialEvaluation pe : partials.get(baseCount))
			collectionQueue.add(new WordBucket(pe));
		while (!collectionQueue.isEmpty()) {
			WordBucket wb = collectionQueue.remove();
			if (wb.pe.cc.done()) {
				stower.handle(wb.dump());
			} else {
				for (PartialEvaluation pe : partials.get(wb.pe.cc)) {
					collectionQueue.add(wb.fill(pe));
				}
			}
		}
		afterCollect.run();
		stower.done(stowerAction);
	}

	private void walk(final int longestWord) {
		beforeWalk.run();
		pool = new ThreadPuddle(threads);
		while (true) {
			while (!work.isEmpty()) {
				Runnable r;
				synchronized (partials) {
					final CharCount cc = work.remove();
					final List<PartialEvaluation> list = new LinkedList<>();
					partials.put(cc, list);
					r = () -> {
						trie.allSingleWordsFromCharacterCount(cc, list);

						// prune the tree
						int[] charCount = new int[cc.counts.length];
						for (PartialEvaluation pe : list) {
							for (int i : pe.charSet()) {
								charCount[i]++;
							}
						}
						int best = 0, bestCount = 0;
						for (int i = 0; i < charCount.length; i++) {
							int bc = charCount[i];
							if (bc == 0)
								continue;
							if (bestCount == 0 || bc < bestCount) {
								best = i;
								bestCount = bc;
							}
						}
						for (Iterator<PartialEvaluation> i = list.iterator(); i.hasNext();) {
							PartialEvaluation pe = i.next();
							if (!pe.charSet().contains(best)) {
								i.remove();
							}
						}

						synchronized (partials) {
							for (PartialEvaluation pe : list) {
								if (!(pe.done() || partials.containsKey(pe.cc))) {
									work.add(pe.cc);
								}
							}
						}
					};
				}
				pool.run(r);
			}
			pool.flush();
			if (work.isEmpty()) {
				break;
			}
		}
		pool.die();
	}
}

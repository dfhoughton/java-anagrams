package dfh.anagrams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
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

	public TrieWalker(Trie trie) {
		this(trie, Runtime.getRuntime().availableProcessors() + 1);
	}

	public TrieWalker(Trie trie, int threads) {
		this.trie = trie;
		pool = new ThreadPuddle(threads);
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

	public Collection<List<String>> anagrams(String phrase) {
		CharCount baseCount = trie.characterCount(phrase);
		if (baseCount == null) {
			return new ArrayList<>();
		}
		synchronized (partials) {
			if (!partials.containsKey(baseCount)) {
				work.add(baseCount);
			}
		}
		walk(baseCount.total);
		return collect(baseCount);
	}

	class WordBucket {
		String word;
		int size = -1;
		WordBucket parent;
		CharCount cc;

		WordBucket(CharCount cc) {
			this.cc = cc;
		}

		int size() {
			if (size == -1) {
				if (parent == null) {
					size = 0;
				} else {
					size = 1 + parent.size();
				}
			}
			return size;
		}

		List<String> dump() {
			List<String> words = new ArrayList<>(size());
			WordBucket n = this;
			while (!(n == null || n.word == null)) {
				words.add(n.word);
				n = n.parent;
			}
			words.sort(null);
			return words;
		}

		WordBucket fill(PartialEvaluation pe) {
			WordBucket wb = new WordBucket(pe.cc);
			wb.parent = this;
			wb.word = pe.translate(trie);
			return wb;
		}
	}

	private Collection<List<String>> collect(CharCount baseCount) {
		beforeCollect.run();
		// de-duping set
		final Set<List<String>> anagrams = new TreeSet<>(new Comparator<List<String>>() {
			public int compare(List<String> a, List<String> b) {
				int i = 0;
				while (true) {
					boolean ab = i >= a.size(), bb = i >= b.size();
					if (ab && bb) {
						return 0;
					}
					if (ab) {
						return -1;
					}
					if (bb) {
						return 1;
					}
					String aw = a.get(i), bw = b.get(i);
					int c = aw.compareTo(bw);
					if (c != 0) {
						return c;
					}
					i++;
				}
			}
		});
		Queue<WordBucket> collectionQueue = new LinkedList<>();
		collectionQueue.add(new WordBucket(baseCount));
		while (!collectionQueue.isEmpty()) {
			WordBucket wb = collectionQueue.remove();
			if (wb.cc.done()) {
				anagrams.add(wb.dump());
			} else {
				for (PartialEvaluation pe : partials.get(wb.cc)) {
					collectionQueue.add(wb.fill(pe));
				}
			}
		}
		afterCollect.run();
		return anagrams;
	}

	private void walk(final int longestWord) {
		beforeWalk.run();
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
						for (PartialEvaluation pe: list) {
							for (int i: pe.charSet()) {
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
	}
}

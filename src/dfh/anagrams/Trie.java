package dfh.anagrams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Trie {
	TrieNode root = new TrieNode();
	CharMap cm = new CharMap();
	StringNormalizer normalizer;

	public Trie(StringNormalizer normalizer) {
		this.normalizer = normalizer;
	}

	public void addWord(String word) {
		word = normalizer.normalize(word);
		if (word.length() > 0) {
			int[] translation = cm.translate(word);
			root.add(translation, 0);
		}
	}

	public void freeze() {
		cm.freeze();
		root.freeze();
	}

	String translate(int[] word) {
		return cm.translate(word);
	}

	public CharCount characterCount(String phrase) {
		return cm.characterCount(normalizer.normalize(phrase));
	}

	/**
	 * Takes a {@link CharCount} and returns a list of {@link PartialEvaluation}
	 * objects.
	 * 
	 * @param cc
	 * @param list
	 */
	public void allSingleWordsFromCharacterCount(CharCount cc, List<PartialEvaluation> list) {
		root.allSingleWordsFromCharacterCount(new PartialEvaluation(cc), list);
		// make sure every character count was decremented somewhere
		Set<Integer> cs = new HashSet<>(cc.charSet().length);
		for (int i: cc.charSet())
			cs.add(i);
		OUTER: for (PartialEvaluation pe : list) {
			for (int i: pe.cc.charSet()) {
				cs.remove(i);
				if (cs.isEmpty())
					break OUTER;
			}
		}
		if (cs.isEmpty()) {
			for (PartialEvaluation pe : list) {
				pe.freeze();
			}
		} else {
			list.clear();
		}
	}

	/**
	 * @return number of nodes in trie
	 */
	public int size() {
		return root.size();
	}

	/**
	 * @return number of terminal nodes in trie
	 */
	public int terminalNodes() {
		return root.terminalNodes();
	}
}

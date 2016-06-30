package dfh.anagrams;

import java.util.ArrayList;
import java.util.Collection;
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
		if (word.length() > 0 ) {
			int[] translation = cm.translate(word);
			root.add(translation, 0);
		}
	}

	public void freeze() {
		cm.freeze();
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
		Set<Integer> cs = cc.charSet();
		for (PartialEvaluation pe : list) {
			Collection<Integer> decremented = cc.decremented(pe.cc);
			cs.removeAll(decremented);
			if (cs.isEmpty()) {
				break;
			}
		}
		if (cs.isEmpty()) {
			for (PartialEvaluation pe : list) {
				pe.freeze();
			}
		} else {
			list.clear();
		}
		((ArrayList<?>) list).trimToSize();
	}

	public int size() {
		return root.size();
	}
}

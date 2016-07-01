package dfh.anagrams;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * A word plus a character count. In the process of calculating anagrams we
 * reduce a character count to a set of smaller character counts coupled with
 * the word we extracted out of the larger character count to make them. This
 * list of partial evaluations is the basic element cached in the dynamic
 * programming algorithm.
 * 
 * @author houghton
 *
 */
public class PartialEvaluation {
	int[] word;
	String w;
	int n = 0;
	boolean frozen = false;
	CharCount cc;
	private Set<Integer> charSet;

	boolean done() {
		return cc.total == 0;
	}

	Set<Integer> charSet() {
		if (charSet == null) {
			charSet = new TreeSet<>();
			for (int i : word)
				charSet.add(i);
		}
		return charSet;
	}
	
	public PartialEvaluation(CharCount cc) {
		word = new int[cc.total];
		this.cc = cc.dup();
	}

	private PartialEvaluation(int[] word, int n, CharCount cc) {
		this.word = Arrays.copyOf(word, word.length);
		this.n = n;
		this.cc = cc.dup();
	}

	public PartialEvaluation dup() {
		return new PartialEvaluation(word, n, cc);
	}

	public PartialEvaluation add(int i) {
		// assert !frozen;
		if (cc.hasAny(i)) {
			PartialEvaluation pe = dup();
			pe.cc.dec(i);
			pe.word[pe.n++] = i;
			return pe;
		}
		return null;
	}

	public String translate(Trie trie) {
		if (w == null) {
			w = trie.translate(word);
		}
		return w;
	}

	/**
	 * Does a little garbage collection.
	 */
	public void freeze() {
		if (!frozen) {
			frozen = true;
			charSet = null;
			word = Arrays.copyOf(word, n);
		}
	}
}

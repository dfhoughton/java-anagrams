package dfh.anagrams;

import java.util.List;

public class TrieNode {
	private TrieNode[] children = new TrieNode[0];
	private int[] jumpList;
	private boolean terminal = false, frozen = false;

	/**
	 * Add a suffix of the given word to the sub-trie rooted at this node,
	 * expanding the trie as necessary.
	 * 
	 * @param translation
	 * @param i
	 */
	public void add(int[] translation, int i) {
		// assert !frozen;
		if (i == translation.length) {
			terminal = true;
		} else {
			int c = translation[i];
			if (c >= children.length) {
				TrieNode[] newChildren = new TrieNode[c + 1];
				System.arraycopy(children, 0, newChildren, 0, children.length);
				children = newChildren;
			}
			TrieNode n = children[c];
			if (n == null) {
				n = new TrieNode();
				children[c] = n;
			}
			n.add(translation, i + 1);
		}
	}

	public int size() {
		int s = 1;
		for (int i : jumpList()) {
			s += children[i].size();
		}
		return s;
	}

	public boolean terminal() {
		return terminal;
	}

	public TrieNode subtree(int i) {
		if (i >= children.length) {
			return null;
		} else {
			return children[i];
		}
	}

	/**
	 * @return the list of non-null child indices
	 */
	public int[] jumpList() {
		if (!frozen) {
			int size = 0;
			for (TrieNode n : children) {
				if (n != null) {
					size++;
				}
			}
			jumpList = new int[size];
			int i = 0;
			for (int j = 0; j < children.length; j++) {
				if (children[j] != null) {
					jumpList[i++] = j;
				}
			}
			frozen = true;
		}
		return jumpList;
	}

	public void allSingleWordsFromCharacterCount(PartialEvaluation pe, List<PartialEvaluation> list) {
		if (terminal) {
			list.add(pe);
		}
		for (int i : jumpList()) {
			PartialEvaluation shorter = pe.add(i);
			if (shorter != null) {
				children[i].allSingleWordsFromCharacterCount(shorter, list);
			}
		}
	}
}

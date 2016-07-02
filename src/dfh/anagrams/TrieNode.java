package dfh.anagrams;

import java.util.List;

public class TrieNode {
	private static TrieNode[] EMPTY_CHILD_LIST = new TrieNode[0];
	private static int[] EMPTY_JUMP_LIST = new int[0];
	private TrieNode[] children = EMPTY_CHILD_LIST;
	private int[] jumpList;
	private boolean terminal = false;

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
				if (children.length > 0) {
					System.arraycopy(children, 0, newChildren, 0, children.length);
				}
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
		for (int i : jumpList) {
			s += children[i].size();
		}
		return s;
	}

	/**
	 * @return the list of non-null child indices
	 */
	public void makeJumpList() {
		if (children.length == 0) {
			jumpList = EMPTY_JUMP_LIST;
		} else {
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
		}
	}

	public void allSingleWordsFromCharacterCount(PartialEvaluation pe, List<PartialEvaluation> list) {
		if (terminal) {
			list.add(pe);
			if (pe.done()) return;
		}
		for (int i : jumpList) {
			PartialEvaluation shorter = pe.add(i);
			if (shorter != null) {
				children[i].allSingleWordsFromCharacterCount(shorter, list);
			}
		}
	}

	/**
	 * @return the number of terminal nodes dominated by this node
	 */
	public int terminalNodes() {
		int n = terminal ? 1 : 0;
		for (int i : jumpList) {
			n += children[i].terminalNodes();
		}
		return n;
	}

	public void freeze() {
		makeJumpList();
		for (int i: jumpList) {
			children[i].freeze();
		}
	}
}

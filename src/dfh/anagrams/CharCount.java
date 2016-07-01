package dfh.anagrams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author houghton
 *
 */
public class CharCount {
	public int[] counts;
	public int total = 0;
	private boolean frozen = false;
	private int hc;
	private int[] charSet;

	public CharCount(int size) {
		counts = new int[size];
	}

	public CharCount dup() {
		return new CharCount(Arrays.copyOf(counts, counts.length), total);
	}

	private CharCount(int[] counts, int total) {
		this.counts = counts;
		this.total = total;
	}

	public boolean done() {
		return total == 0;
	}

	/**
	 * Increments the count, if possible
	 * 
	 * @param i
	 *            integer corresponding to a character
	 * @return whether the specified letter is known
	 */
	public boolean inc(int i) {
		// assert !frozen;
		if (i >= counts.length) {
			return false;
		}
		counts[i]++;
		total++;
		return true;
	}

	/**
	 * Decrements specified character count and total.
	 * 
	 * @param i
	 *            integer corresponding to character
	 * @return whether decrementing was possible
	 */
	public boolean dec(int i) {
		// assert !frozen;
		if (counts[i] == 0) {
			return false;
		}
		counts[i]--;
		total--;
		return true;
	}

	public boolean hasAny(int i) {
		return counts[i] > 0;
	}

	public boolean equals(Object o) {
		try {
			CharCount cc = (CharCount) o;
			if (total != cc.total) {
				return false;
			}
			for (int i = 0; i < counts.length; i++) {
				if (counts[i] != cc.counts[i]) {
					return false;
				}
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	public int hashCode() {
		if (!frozen) {
			hc = Arrays.hashCode(counts);
			frozen = true;
		}
		return hc;
	}

	/**
	 * @return the set of characters counted
	 */
	public int[] charSet() {
		if (charSet == null) {
			Set<Integer> set = new TreeSet<>();
			for (int i = 0; i < counts.length; i++) {
				int c = counts[i];
				if (c > 0) {
					set.add(i);
				}
			}
			charSet = new int[set.size()];
			int i = 0;
			for (int c: set) {
				charSet[i++] = c;
			}
		}
		return charSet;
	}

	/**
	 * @param cc
	 * @return list of all the characters whose counts were decremented between
	 *         this and the other
	 */
	public Collection<Integer> decremented(CharCount cc) {
		List<Integer> list = new ArrayList<>(counts.length);
		for (int i = 0; i < counts.length; i++) {
			int c = counts[i];
			if (c > 0 && cc.counts[i] < c) {
				list.add(i);
			}
		}
		return list;
	}
}

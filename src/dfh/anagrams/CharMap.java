package dfh.anagrams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CharMap {
	private int[] c2i;
	private int charOffset;
	private char[] i2c;

	public CharMap(Map<Character, int[]> frequencyMap) {
		List<Character> list = new ArrayList<>(frequencyMap.keySet());
		Comparator<Character> c = (a, b) -> frequencyMap.get(b)[0] - frequencyMap.get(a)[0];
		list.sort(c);
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (char ch : list) {
			int i = ch;
			if (i > max)
				max = i;
			if (i < min)
				min = i;
		}
		charOffset = min;
		c2i = new int[1 + max - min];
		i2c = new char[list.size() + 1]; // reserve 0 as a null character
		for (int i = 1; i <= list.size(); i++) {
			char ch = list.get(i - 1);
			i2c[i] = ch;
			c2i[ch - charOffset] = i;
		}
	}

	public int[] translate(String word) {
		int[] translation = new int[word.length()];
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			int ci = charToInt(c);
			// assert ci != 0;
			translation[i] = ci;
		}
		return translation;
	}

	public String translate(int[] word) {
		StringBuffer b = new StringBuffer(word.length);
		for (int i : word) {
			char c = intToChar(i);
			// assert c != (char) 0;
			b.append(c);
		}
		return b.toString().intern();
	}

	/**
	 * @param c
	 * @return corresponding integer; 0 if the character could not be translated
	 */
	public int charToInt(char c) {
		int i = c - charOffset;
		if (i < 0)
			return 0;
		if (i >= c2i.length)
			return 0;
		return c2i[i];
	}

	/**
	 * Converts a word to a {@link CharCount}
	 * 
	 * @param word
	 * @return {@link CharCount}, or null if not all characters are translatable
	 */
	public CharCount characterCount(String word) {
		CharCount cc = new CharCount(i2c.length);
		for (char c : word.toCharArray()) {
			int i = charToInt(c);
			if (i == 0) {
				return null;
			}
			if (!cc.inc(i)) {
				return null;
			}
		}
		return cc;
	}

	public char intToChar(int i) {
		if (i < 0 || i >= i2c.length) {
			return (char) 0;
		}
		return i2c[i];
	}

}

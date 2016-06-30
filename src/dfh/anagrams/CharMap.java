package dfh.anagrams;

import java.util.Arrays;

public class CharMap {
	private int[] c2i;
	private int charOffset = -1;
	private char[] i2c;
	private boolean frozen = false;

	public void freeze() {
		frozen = true;
	}

	public int[] translate(String word) {
		int[] translation = new int[word.length()];
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			translation[i] = charToInt(c);
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
	 * @return corresponding integer; -1 if the character could not be
	 *         translated
	 */
	public int charToInt(char c) {
		int rv, i = (int) c;
		if (resizeAsNeeded(i)) {
			i -= charOffset;
			rv = c2i[i];
			if (rv == -1) {
				if (frozen) {
					return -1;
				}
				rv = i2c.length;
				char[] newIntToChar = new char[rv + 1];
				System.arraycopy(i2c, 0, newIntToChar, 0, rv);
				newIntToChar[rv] = c;
				i2c = newIntToChar;
				c2i[i] = rv;
			}
			return rv;
		} else {
			return -1;
		}
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
			if (i == -1) {
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

	private boolean resizeAsNeeded(int i) {
		if (charOffset == -1) {
			if (frozen) {
				return false;
			}
			charOffset = i;
			c2i = new int[] { -1 };
			i2c = new char[0];
		} else if (i > charOffset) {
			int delta = i - charOffset;
			if (delta >= c2i.length) {
				if (frozen) {
					return false;
				}
				int[] newCharToInt = new int[c2i.length + delta];
				Arrays.fill(newCharToInt, -1);
				System.arraycopy(c2i, 0, newCharToInt, 0, c2i.length);
				c2i = newCharToInt;
			}
		} else if (i < charOffset) {
			if (frozen) {
				return false;
			}
			int delta = charOffset - i;
			int[] newCharToInt = new int[c2i.length + delta];
			Arrays.fill(newCharToInt, -1);
			System.arraycopy(c2i, 0, newCharToInt, delta, c2i.length);
			c2i = newCharToInt;
			charOffset = i;
		}
		return true;
	}

}

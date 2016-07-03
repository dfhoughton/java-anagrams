package dfh.anagrams;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class UniqStower extends AnagramStower {

	public UniqStower(PrintStream out) {
		super(out);
	}

	Set<String> known = new TreeSet<>();

	@Override
	public int size() {
		return known.size();
	}

	@Override
	public void handle(List<String> anagram) {
		if (!test.test()) {
			StringBuffer b = new StringBuffer();
			int last = anagram.size() - 1;
			for (int i = 0; i < anagram.size(); i++) {
				String word = anagram.get(i);
				b.append(word);
				if (i != last)
					b.append(' ');
			}
			String s = b.toString();
			if (!known.contains(s)) {
				known.add(s);
				out.println(s);
			}
		}
	}

	@Override
	public void done(Runnable beforeDone) {
		out.flush();
	}

}

package dfh.anagrams;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SortedUniqStower extends AnagramStower {
	
	public SortedUniqStower(PrintStream out) {
		super(out);
	}

	final static Comparator<List<String>> cmp = (a, b) -> {
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
	};
	Set<List<String>> anagrams = new TreeSet<>(cmp);

	@Override
	public int size() {
		return anagrams.size();
	}

	@Override
	public void handle(List<String> anagram) {
		if (!test.test())
			anagrams.add(anagram);
	}

	@Override
	public void done(Runnable beforeDone) {
		beforeDone.run();
		for (List<String> anagram : anagrams) {
			int last = anagram.size() - 1;
			for (int i = 0; i < anagram.size(); i++) {
				String word = anagram.get(i);
				out.print(word);
				if (i == last)
					out.println();
				else
					out.print(' ');
			}
		}
		out.flush();
	}

}

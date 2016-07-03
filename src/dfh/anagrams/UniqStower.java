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
			for (String s : anagram)
				b.append(s).append(' ');
			String s = b.toString().trim();
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

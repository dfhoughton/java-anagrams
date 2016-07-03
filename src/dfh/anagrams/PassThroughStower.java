package dfh.anagrams;

import java.io.PrintStream;
import java.util.List;

public class PassThroughStower extends AnagramStower {
	int n = 0;

	public PassThroughStower(PrintStream out) {
		super(out);
	}

	@Override
	public void handle(List<String> anagram) {
		if (!test.test()) {
			int last = anagram.size() - 1;
			for (int i = 0; i < anagram.size(); i++) {
				String word = anagram.get(i);
				out.print(word);
				if (i == last)
					out.println();
				else
					out.print(' ');
			}
			n += 1;
		}
	}

	@Override
	public void done(Runnable beforeDone) {
		beforeDone.run();
		out.flush();
	}

	@Override
	int size() {
		return n;
	}

}

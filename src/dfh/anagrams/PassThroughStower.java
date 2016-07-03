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
			for (String word : anagram) {
				out.print(word);
				out.print(' ');
			}
			out.println();
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

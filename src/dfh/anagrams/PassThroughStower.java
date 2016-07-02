package dfh.anagrams;

import java.io.PrintStream;
import java.util.List;

public class PassThroughStower implements AnagramStower {
	
	PrintStream out;
	
	public  PassThroughStower(PrintStream out) {
		this.out = out;
	}

	@Override
	public void handle(List<String> anagram) {
		for (String word : anagram) {
			out.print(word);
			out.print(' ');
		}
		out.println();
	}

	@Override
	public void done(Runnable beforeDone) {
		beforeDone.run();
		out.flush();
	}

}

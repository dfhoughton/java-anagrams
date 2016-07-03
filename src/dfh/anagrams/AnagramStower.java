package dfh.anagrams;

import java.io.PrintStream;
import java.util.List;

abstract class AnagramStower {
	PrintStream out;
	protected Test test = () -> false;

	public AnagramStower(PrintStream out) {
		this.out = out;
	}

	public void setTest(Test test) {
		this.test = test;
	}

	interface Test {
		boolean test();
	}

	private boolean done = false;

	public void finished() {
		this.done = true;
	}

	abstract void handle(List<String> anagram);

	abstract void done(Runnable beforeDone);

	abstract int size();

	public boolean done() {
		return done;
	}
}

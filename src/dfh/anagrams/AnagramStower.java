package dfh.anagrams;

import java.util.List;

public interface AnagramStower {
	public void handle(List<String> anagram);

	public void done(Runnable beforeDone);
}

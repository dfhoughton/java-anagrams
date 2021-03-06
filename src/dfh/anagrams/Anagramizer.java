package dfh.anagrams;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dfh.cli.Cli;
import dfh.cli.coercions.FileCoercion;
import dfh.cli.rules.IntSet;
import dfh.cli.rules.Range;

public class Anagramizer {
	static PrintStream out = System.out;

	public static void main(String[] args) {
		Object[][][] spec = {
				//
				{ { Cli.Opt.USAGE, "extract anagrams from a phrase" }, { "usage.txt" } }, //
				{ { Cli.Opt.ARGS, "word", Cli.Opt.PLUS } }, //
				{ { Cli.Opt.NAME, Anagramizer.class.getCanonicalName() } }, //
				{ { Cli.Opt.VERSION, "0.0.1" } }, //
				{ { "dictionary", 'd', FileCoercion.C }, { "word list; one word per line" }, { Cli.Res.REQUIRED } }, //
				{ { "out", 'o', FileCoercion.C }, { "file for output" } }, //
				{ { "verbose", 'v' }, { "provide progress information" } }, //
				{ { "time", 't' }, { "if verbose, time operations" } }, //
				{ { "uniq", 'u' }, { "return unique anagrams" } }, //
				{ { "sort", 's' }, { "return a *sorted* list of *unique* anagrams" } }, //
				{ { "shuffle", Integer.class, 0 },
						{ "shuffle generation order; useful with --limit; 0 = no shuffling; 1 = shuffle some; 2 = shuffle always" },
						{ new IntSet(0, 1, 2) } }, //
				{ { "limit", 'l', 'n', Integer.class }, { "return at most this many anagrams" }, { Range.positive() } }, //
				{ { "threads", Integer.class, Runtime.getRuntime().availableProcessors() + 1 },
						{ "maximum number of threads" }, { Range.positive() } },//
		};
		Cli cli = new Cli(spec);
		cli.parse(args);
		File dictionary = (File) cli.object("dictionary"), outFile = (File) cli.object("out");
		int threads = cli.integer("threads");
		Integer limit = cli.integer("limit");
		if (!(dictionary.exists() && dictionary.isFile())) {
			cli.die("The provided dictionary, " + dictionary + ", does not appear to be a word list.");
		}
		if (outFile != null) {
			try {
				out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)), true);
			} catch (FileNotFoundException e) {
				cli.die("could not write to " + outFile);
			}
		}
		final boolean verbose = cli.bool("verbose"), timeOperations = cli.bool("time");
		StringNormalizer normalizer = (word) -> word.replaceAll("[^\\p{L}\\p{Nd}]+", "").toLowerCase();
		StringBuffer buffer = new StringBuffer();
		for (String s : cli.slurpedArguments()) {
			buffer.append(s).append(' ');
		}
		String phrase = buffer.toString().trim();
		final long[] time = { 0 }, firstTime = { 0 };

		if (verbose) {
			if (timeOperations) {
				firstTime[0] = time[0] = System.currentTimeMillis();
			}
			System.out.println("reading dictionary...");
		}
		Trie trie;
		{ // construction scope for temporary data structures
			Map<Character, int[]> frequencyMap = new TreeMap<>();
			List<String> words = new LinkedList<>();
			try (BufferedReader br = new BufferedReader(new FileReader(dictionary))) {
				for (String line; (line = br.readLine()) != null;) {
					String word = normalizer.normalize(line);
					words.add(word);
					for (char c : word.toCharArray()) {
						int[] counts = frequencyMap.get(c);
						if (counts == null)
							frequencyMap.put(c, counts = new int[] { 0 });
						counts[0]++;
					}
				}
			} catch (FileNotFoundException e) {
				cli.die("could not find " + dictionary);
			} catch (IOException e) {
				cli.die("IO exception while reading " + dictionary);
			}
			CharMap cm = new CharMap(frequencyMap);
			trie = new Trie(normalizer, cm);
			for (String word : words)
				trie.addWord(word);
			trie.freeze();
		}

		if (verbose) {
			if (timeOperations) {
				reportTiming(time[0]);
			}
			int s = trie.size(), s2 = trie.terminalNodes();
			System.out.printf("trie has %,d %s, %,d %s\n", s, inflect("node", s), s2, inflect("terminal node", s2));
			System.out.printf("collecting anagrams of '%s'...\n", phrase);
			if (timeOperations) {
				time[0] = System.currentTimeMillis();
			}
		}
		trie.freeze();
		AnagramStower stower;
		Runnable stowerAction;
		if (cli.bool("sort")) {
			final SortedUniqStower as = (SortedUniqStower) (stower = new SortedUniqStower(out));
			stowerAction = () -> {
				if (verbose) {
					System.out.printf("%,d %s found\n\n", as.size(), inflect("anagram", as.size()));
				}
			};
		} else if (cli.bool("uniq")) {
			final UniqStower as = (UniqStower) (stower = new UniqStower(out));
			stowerAction = () -> {
				if (verbose) {
					System.out.printf("%,d %s found\n\n", as.size(), inflect("anagram", as.size()));
				}
			};
		} else {
			stower = new PassThroughStower(out);
			stowerAction = () -> {
			};
		}
		if (limit != null)
			stower.setTest(() -> stower.size() >= limit);
		TrieWalker walker = new TrieWalker(trie, stower, threads);
		if (verbose) {
			walker.beforeWalk = () -> {
				System.out.println("collecting all necessary partial evaluations...");
			};
			walker.beforeCollect = () -> {
				int ncc = walker.numberCharacterCounts();
				int npe = walker.numberPartialEvaluations();
				System.out.printf("%,d %s, %,d %s\n", ncc, inflect("character count", ncc), npe,
						inflect("partial evaluation", npe));
				if (timeOperations) {
					reportTiming(time[0]);
				}
				System.out.println("walking partial evaluation tree...");
				if (timeOperations) {
					time[0] = System.currentTimeMillis();
				}
			};
			walker.afterCollect = () -> {
				System.out.println("DONE!");
				if (timeOperations) {
					reportTiming(time[0]);
					System.out.println("total elapsed time:");
					reportTiming(firstTime[0]);
				}
				System.out.println();
			};
			walker.beforeClean = () -> {
				System.out.println("removing unused branches...");
			};
			walker.afterClean = (a,b,c) -> {
				System.out.printf("initial branches: %,d; final branches: %,d; removed: %,d; character counts removed: %,d\n", a, b, a-b, c);
			};
		}
		boolean shuffle = cli.integer("shuffle") > 0, shuffleWell = cli.integer("shuffle") == 2;
		walker.anagrams(phrase, stowerAction, shuffle, shuffleWell);
	}

	private static void reportTiming(long time) {
		time = System.currentTimeMillis() - time;
		long milliseconds = time;
		long seconds = milliseconds / 1000l;
		milliseconds %= 1000;
		long minutes = seconds / 60l;
		seconds %= 60;
		long hours = minutes / 60l;
		minutes %= 60;
		System.out.printf("\t%,d %s (%d)", time, inflect("millisecond", time), time);
		boolean foundFirst = false;
		if (hours > 0) {
			if (!foundFirst) {
				System.out.print(';');
				foundFirst = true;
			}
			System.out.printf(" %,d %s", hours, inflect("hour", hours));
		}
		if (foundFirst || minutes > 0) {
			if (!foundFirst) {
				System.out.print(';');
				foundFirst = true;
			}
			System.out.printf(" %d %s", minutes, inflect("minute", minutes));
		}
		if (foundFirst || seconds > 0) {
			if (!foundFirst) {
				System.out.print(';');
				foundFirst = true;
			}
			System.out.printf(" %d %s", seconds, inflect("second", seconds));
		}
		if (foundFirst) {
			System.out.printf(" %d %s", milliseconds, inflect("millisecond", milliseconds));
		}
		System.out.println();
	}

	private static String inflect(String word, long n) {
		return inflect(word, (int) n);
	}

	private static String inflect(String word, int n) {
		return n == 1 ? word : word + "s";
	}

}

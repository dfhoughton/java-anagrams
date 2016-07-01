package dfh.anagrams;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import dfh.cli.Cli;
import dfh.cli.coercions.FileCoercion;
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
				{ { "threads", Integer.class, Runtime.getRuntime().availableProcessors() + 1 },
						{ "maximum number of threads" }, { Range.positive() } },//
		};
		Cli cli = new Cli(spec);
		cli.parse(args);
		File dictionary = (File) cli.object("dictionary"), outFile = (File) cli.object("out");
		int threads = cli.integer("threads");
		if (!(dictionary.exists() && dictionary.isFile())) {
			cli.die("The provided dictionary, " + dictionary + ", does not appear to be a word list.");
		}
		if (outFile != null) {
			try {
				out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
			} catch (FileNotFoundException e) {
				cli.die("could not write to " + outFile);
			}
		}
		final boolean verbose = cli.bool("verbose"), timeOperations = cli.bool("time");
		StringNormalizer normalizer = new StringNormalizer() {
			@Override
			public String normalize(String word) {
				return word.replaceAll("[^\\p{L}\\p{Nd}]+", "").toLowerCase();
			}
		};
		StringBuffer buffer = new StringBuffer();
		for (String s : cli.slurpedArguments()) {
			buffer.append(s).append(' ');
		}
		String phrase = buffer.toString().trim();
		Trie trie = new Trie(normalizer);
		final long[] time = { 0 }, firstTime = {0};
		if (verbose) {
			if (timeOperations) {
				firstTime[0] = time[0] = System.currentTimeMillis();
			}
			System.out.println("reading dictionary...");
		}
		try (BufferedReader br = new BufferedReader(new FileReader(dictionary))) {
			for (String line; (line = br.readLine()) != null;) {
				trie.addWord(line);
			}
		} catch (FileNotFoundException e) {
			cli.die("could not find " + dictionary);
		} catch (IOException e) {
			cli.die("IO exception while reading " + dictionary);
		}
		trie.freeze();
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
		TrieWalker walker = new TrieWalker(trie, threads);
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
		}
		Collection<List<String>> anagrams = walker.anagrams(phrase);
		if (verbose) {
			System.out.printf("%,d %s found\n\n", anagrams.size(), inflect("anagram", anagrams.size()));
		}
		for (List<String> words : anagrams) {
			for (String word : words) {
				out.print(word);
				out.print(' ');
			}
			out.println();
		}
		out.flush();
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
		if (n == 1) {
			return word;
		} else {
			return word + "s";
		}
	}

}

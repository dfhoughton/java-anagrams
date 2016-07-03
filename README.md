# java-anagrams
ye olde anagram algorithm, this time in Java

I initially created an anagram generation algorithm in Perl. That was a fine algorithm, better
in some ways than this one, due to the easy accessibility of closures in Perl. Then, to get a
job (which I got, thank Ceiling Cat), I rewrote it in Ruby, along with a lovely social Rails app for
sharing anagrams. Then I thought I would re-write it in Rust to add multi-threading and make it really,
really fast (though both the Ruby and Perl versions were generally as fast as I needed them to be). But,
I thought, before I bogged myself down in a Rust wrestling match I should verify the multi-threaded
algorithm (and play with Java lambdas), so I wrote this.

This Java code is dependent on two of my other open source Java libraries: dfh.thread.ThreadPuddle and
dfh.cli. If you've got them, you can create a command line utility whose help information is as follows:

```
~ $ anagrams --help
USAGE: dfh.anagrams.Anagramizer [options] <word>+

  extract anagrams from a phrase

    --dictionary -d <file>  word list; one word per line; REQUIRED
    --out -o        <file>  file for output
    --verbose -v            provide progress information
    --time -t               if verbose, time operations
    --uniq -u               return unique anagrams
    --sort -s               return a *sorted* list of *unique* anagrams
    --shuffle       <int>   shuffle generation order; useful with --limit; 0 =
                            no shuffling; 1 = shuffle some; 2 = shuffle always; 
                            value must be in {0, 1, 2}; default: 0
    --limit -l -n   <int>   return at most this many anagrams; value must be > 0
    --threads       <int>   maximum number of threads; value must be > 0;
                            default: 9

    --version               print dfh.anagrams.Anagramizer version
    --help -? -h            print usage information

This utility allows you to generate all the possible anagrams of a phrase given 
a particular word list and the following restrictions:

1. case is normalized to lowercase

2. all non-word characters are stripped away

3. two arrangements of the same words are considered the same anagram

The longer the phrase you seek to extract anagrams from, the more memory you will
need. You can save memory by not requiring that only unique or sorted anagrams be
returned, as this requires that anagrams be passed through a sorted set first. If
you sort, all the anagrams are returned at the end. If you only require 
uniqueness, they will be streamed out as they are discovered.
```

This is indeed somewhat faster than the Perl or Ruby versions, thanks to its use of
many threads to explore the problem space. It being Java, it also requires vast
amounts of memory, much more than either of the previous two versions. In fact, some
phrases that were tractable in Perl are no longer tractable for me because my machine,
with a mere 12 gigs of memory, does not have sufficient memory to complete the job.
Anyway, here it is. You will need a word list for it to work from, and you may want
to tinker with its string normalization algorithm if it does not suit whatever
language you're working with -- currently it drops all non-word, non-numeric characters, but
Java's definition of what these are, `[^\p{L}\p{Nd}]`, might not suit you.

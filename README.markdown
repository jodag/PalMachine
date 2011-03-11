# PalMachine

PalMachine was inspired by an ITA Software Challenge: Given a dictionary, find the shortest palindrome that is also a pangram (contains all letters of the alphabet).  See http://www.itasoftware.com/careers/puzzle_archive.html .

PalMachine explores the palindromes that can be created from the dictionary, through use of a Finite State Machine (FSM).  Given a dictionary, it computes a FSM such that any walk from the start state to the end state is a palindrome.

More specifically:
1. Any walk from the start state to the end state produces a palindrome.
2. Any palindrome which can be created from the dictionary has a corresponding walk.

Thus, we can use the FSM (and standard FSM algorithms) to answer any question about palindromes.

# Background

The software challenge brings to play both pangrams and palindromes.  Surprisingly, the problem of finding short pangrams is well studied: it's known as the Set Cover Problem, and is NP Hard.  What interests me more is the palindrome problem, which, to my knowledge, hasn't been studied.

Generating palindromes is fairly easy: We index both the dictionary and the reversed dictionary using a trie (prefix tree).  We pick a word from the dictionary for the left side, and a compatible word from the reversed dictionary the right side.  For instance:

    "SOME... ...mEMOS"

Since the right side has an unmatched 'm', we now pick any word from the dictionary which begins with 'm' (for instance, 'men', yielding "SOME Men... ...MEMOS"), and continue until we meet in the middle, with at most one unmatched character.

The difficulty is that the set of possible palindromes is _infinite_; even if we restrict it to palindromes of less than a certain length, the set grows too fast to be tractable.  The question I wanted to answer is: Can we characterize the entire set of possible palindromes in a tractable way which can be used to solve problems?

While experimenting, I noticed that we often reach the same state repeatedly.  For instance, we will often have one unmatched 'm' on the right side, which I designate as state '-m', or an unmatched 'en' on the left side, which I designate as state '+en'.  This state completely determines our possible choices for the next word - it doesn't matter what words we used to get there.  This observation, and the algebra behind it, is the basis of the FSM.

# Implementation

A lot of the computation can be done using relational algebra.  Instead of reinventing the wheel, I use Postgres for that, and use Scala 2.7 for the nonrelational logic.  The key classes are:

* PalSeq, PalState, and Transition: The algebra behind the FSM.
* Fsm: Computes the FSM using the above classes.
* db package: A thin access layer to the RDBMS.

The code can be best followed by looking at the above classes in that order.

The FSM itself is represented as a graph stored in the database.  It can easily be dumped as a single CSV file as well.

# Usage

The FSM is a toolkit capable of answering any questions about the set of all possible palindromes.  It is a finite, tractable representation of that set, using standard graph notation.  The ITA Software Dictionary of 170k words (60k words after being preprocessed) results in an FSM with about 4k states.  See the file FSM.markdown for more information.

This code creates the FSM, and stores it in the database.

## Running the Code

For simplicity, all configuration is done directly via Scala code.

The code (and tests) requires a Postgres database.  By default, it will use db: palmachine user: palmachine pw: palmachine 
, but this can be changed in DataConnection.scala.

After creating the database, run class db.DataInit to create the schema and load the built in test wordlist.

You can then run the tests.  To generate the FSM, run class Fsm.  The FSM will be saved in the database.

For interesting results, use a large wordlist, such as the included wordfile.txt, by running:
	db.DataInit "path-to-wordfile.txt"


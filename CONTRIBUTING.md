# Contributing

## Build

### Sbt Projects

- `compat` project: implementation of the compatibility library ;
- `scalafix*`: implementation of the migration tool.

## Migration tool

Several levels of contribution are possible!

### Report a missing case

Create an issue tagged with the
[migration](https://github.com/scala/collection-strawman/labels/migration) label.
Embrace `diff`s to describe differences between the standard collections and
the strawman:

~~~ diff
- xs.toIterator
+ xs.iterator
~~~

### Add a missing test case

Even better, instead of providing a diff, you can directly add it as a test case!

1. Fork this repository and create a separate branch;

2. Add a file in the `scalafix/input/src/main/scala/fix/` directory with code
   that uses the standard collections:

~~~ scala
class toIteratorVsIterator(xs: Iterable[Int]) {
  xs.toIterator
}
~~~

3. Add a corresponding file in the `scalafix/output213/src/main/scala/fix/` directory
   with the same code but using the strawman:

~~~ scala
import strawman.collection.Iterable

class toIteratorVsIterator(xs: Iterable[Int]) {
  xs.iterator
}
~~~

4. Check that your code example compiles
    - run sbt
      and then run the following task `compile`;

5. Commit your changes, push your branch to your fork and create a pull request.

Then maybe someone will take over and implement your use case… or maybe you will
(see next section)!

### Implement a missing case

Even better, complete the migration tool implementation to support the missing case!

After you have added the missing case (see previous section), run the following
sbt task (with sbt started from the `scalafix/` directory) to run the
migration tool on the input files and check whether the result matches the
expected output files:

~~~
> scalafix-tests/test
~~~

Fix the implementation of the rule (in the
`rules/src/main/scala/fix/NewCollections.scala` file) until the
tests are green. You can find more help about the scalafix API in its
[documentation](https://scalacenter.github.io/scalafix/docs/rule-authors/setup).

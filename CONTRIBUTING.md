# Contributing

## Build

### Sbt Projects

- `compat` project: implementation of the compatibility library ;
- `scalafix*`: implementation of the migration tool.
- `binary-compat`: preserve binary compatibility when using the compat library on 2.12

## Migration tool

Several levels of contribution are possible!

### Report a missing case

Create an issue [scala-collection-compat/issues](https://github.com/scala/scala-collection-compat/issues).
Embrace `diff`s to describe differences between the standard collections and
the new collection:

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
class ToIteratorVsIteratorSrc(xs: Iterable[Int]) {
  xs.toIterator
}
~~~

3. Add a corresponding file in the `scalafix/output/src/main/scala/fix/` directory
   with the same code but using the new collection:

~~~ scala
import scala.collection.compat._

class ToIteratorVsIteratorSrc(xs: Iterable[Int]) {
  xs.iterator
}
~~~

4. Check that your code example compiles
    - run sbt
      and then run the following task `compile`;

5. Format your code with the `scalafmt` sbt task.

6. Commit your changes, push your branch to your fork and create a pull request.

Then maybe someone will take over and implement your use case… or maybe you will
(see next section)!

### Implement a missing case

Even better, complete the migration tool implementation to support the missing case!

After you have added the missing case (see previous section), run the following
sbt task to run the migration tool on the input files and check whether the result matches the
expected output files:

~~~
> scalafix-tests/test
~~~

Fix the implementation of the rule (in the `rules/src/main/scala/fix/NewCollections.scala` file) until the
tests are green. You can find more help about the scalafix API in its
[documentation](https://scalacenter.github.io/scalafix/docs/rule-authors/setup).


### Scalafix Teskit Directory Layout


```
+------------------+-----+-----------------+
|                  |Input|     Output      |
|                  | 2.12| 2.11| 2.12| 2.13|
+------------------+-----+-----+-----+-----+
|data              |  X  |  X  |  X  |  X  |
|input             |  X  |     |     |     |
|output            |     |  X  |  X  |  X  |
|output212         |     |     |  X  |     |
|output212+        |     |     |  X  |  X  |
|output213         |     |     |     |  X  |
|output213-failure |     |     |     |  X  |
+------------------+-----+-----+-----+-----+

rules: Rule implementations
tests: Scalafix testkit launcher (useful to run a single input file)
```

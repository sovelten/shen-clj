# Shen-clj

A Clojure port of the Shen Language

* Official website: http://www.shenlanguage.org/
* Shen sources: https://github.com/Shen-Language/shen-sources

## Status

The port does not yet conform to OS Kernel 21.0 test suite, but most tests are passing.

## Generating Clojure source

To generate the clojure source from the KLambda source:

```
lein run
```

This will overwrite `src/shen/functions.clj`

## Running

To run the shen repl:

```
lein shen-clj
```

## Compiling

To build the standalone `jar` file:

```
lein make
```

## Running the test suite

After loading the repl, type:

```
(cd "<repo_path>/resources/shen/tests")
(load "README.shen")
(load "tests.shen")
```

## Known issues

* No tail call optimization
* REPL behaves erratically when type checking is turned on.

# SimpleBC

A simplified version of bc (basic calculator) using ANTLR 4 for the Programming Language Concepts class

## Getting Started

### Installing

This project requires:
- [OpenJDK](https://openjdk.java.net/) version 8
- [Maven](https://maven.apache.org/) 3.5.4
- [ANTLR4](https://www.antlr.org/) ([Instructions here](https://github.com/antlr/antlr4/blob/master/doc/getting-started.md)).

### Compiling

To compile (or run the VSCode Build task):

```bash
mvn package
```

> Note: If you just want to generate the ANTLR code, run this instead `mvn antlr4:antlr4`

### Running

To run from the terminal:

```bash
java -cp target/SimpleBC-1.0-jar-with-dependencies.jar com.codecaptured.SimpleBC.SimpleBC
```

Return and press `ctrl+D` once done to get the output.

To run from a file:

```bash
java -cp target/SimpleBC-1.0-jar-with-dependencies.jar com.codecaptured.SimpleBC.SimpleBC < test/scratchpad.bc
```

### Testing

To automatically test each of the `*-input.bc` and `*-output.txt` pairs in `test/auto/`. Run the below commands

```bash
./test-all.sh
```

It will output if each test Passes or Fails.

Files currently not automatically tested: `read-function.bc`, `print-function.bc`

Also, the `scratchpad.bc` is configured to be the automatic test file for the VSCode debugger (press "F5").

<!-- TODO: Get `mvn testing` and JUnit tests working -->

## Other Info

### Arbitrary Precision

This version of SimpleBC uses the BigDecimal class internally, instead of the double primitive. Just like bc, the scale is controlled with an internal variable. By default, it is set to 20.

The following code produces the same output in both bc -l and SimpleBC:

```
> scale
20
> 1 / 3
.33333333333333333333
> scale = 3
> scale
3
> 1 / 3
0.333
```

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

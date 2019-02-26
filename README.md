# Simple bc

A simplified version of bc (basic calculator) using ANTLR 4 for the Programming Language Concepts class

## Getting Started

### Installing

Have OpenJDK version 8 installed and follow [these instructions](https://github.com/antlr/antlr4/blob/master/doc/getting-started.md) for installing ANTLR 4.

### Simple Example

To compile and run a simple calculator, follow these steps

```bash
cd src/
antlr4 SimpleBC.g4
javac SimpleBC*.java
grun SimpleBC exprList -tree ../test/scratchpad.bc
```

### Testing

To automatically test each of the `*-input.bc` and `*-output.txt` pairs in `test/auto/`. Run the below commands

```bash
cd src/
./test-all.sh
```

It will output if each test Passes or Fails.

Files currently not automatically tested: `read-function.bc`, `print-function.bc`

Also, the `scratchpad.bc` is configured to be the automatic test file for the VSCode debugger (press "F5").

## Other Info

### Arbitrary Precision

This version of simple-bc uses the BigDecimal class internally, instead of the double primitive. Just like bc, the scale is controlled with an internal variable. By default, it is set to 20.

The following code produces the same output in both bc -l and simple-bc:

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

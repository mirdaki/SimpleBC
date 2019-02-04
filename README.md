# Simple bc

A simplified version of bc (basic calculator) using ANTLR 4 for the Programming Language Concepts class

## Getting Started

### Installing

Have OpenJDK version 8 installed and follow [these instructions](https://github.com/antlr/antlr4/blob/master/doc/getting-started.md) for installing ANTLR 4.

> Note: Make sure ANTLR is in the classpath with `export CLASSPATH=".:/usr/local/lib/antlr-4.7.1-complete.jar:$CLASSPATH"`

### Simple Example

To compile and run a simple calculator, follow these steps

```bash
cd src/
antlr4 SimpleBC.g4
javac SimpleBC*.java
grun SimpleBC exprList -tree ../test/scratchpad.bc
```

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

# Simple bc

A simplified version of bc (basic calculator) using ANTLR 4 for the Programming Language Concepts class

## Getting Started

### Installing

Have OpenJDK version 8 installed and follow [these instructions](https://github.com/antlr/antlr4/blob/master/doc/getting-started.md) for installing ANTLR 4.

> Note: Make sure ANTLR is in the classpath with `export CLASSPATH=".:/usr/local/lib/antlr-4.7.1-complete.jar:$CLASSPATH"`

### Simple Example

To compile and run a simple calculator, follow these steps

```bash
antlr4 Calculator.g4
javac Calculator*.java
grun Calculator expr -tree test/input.txt
```

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

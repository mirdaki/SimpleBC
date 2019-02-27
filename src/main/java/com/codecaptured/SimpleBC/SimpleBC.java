package com.codecaptured.SimpleBC;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.InputStream;

public class SimpleBC {
	public static void main(String[] args) throws Exception {
		String inputFile = null;
		if (args.length > 0)
			inputFile = args[0];
		InputStream is = System.in;
		if (inputFile != null)
			is = new FileInputStream(inputFile);
		ANTLRInputStream input = new ANTLRInputStream(is);
		SimpleBCLexer lexer = new SimpleBCLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SimpleBCParser parser = new SimpleBCParser(tokens);
		ParseTree tree = parser.prog(); // parse

		EvalVisitor eval = new EvalVisitor();
		eval.visit(tree);
	}
}

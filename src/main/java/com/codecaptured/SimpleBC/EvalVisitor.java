package com.codecaptured.SimpleBC;

import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayDeque;
import java.math.BigDecimal;
import java.lang.RuntimeException;

public class EvalVisitor extends SimpleBCBaseVisitor<BigDecimal> {

	/* Utilities and environment */

	// Input for functions
	public static Scanner input = new Scanner(System.in);

	// Create environment
	public static class Environment {
		public HashMap<String, Function> functionMap;
		public HashMap<String, BigDecimal> variableMap;

		public Environment(HashMap<String, Function> functionMap, HashMap<String, BigDecimal> variableMap) {
			this.functionMap = functionMap;
			this.variableMap = variableMap;
		}
	}
	public interface Function {
		public BigDecimal execute(BigDecimal arg);
	}

	public static ArrayDeque<Environment> programEnvrioment = new ArrayDeque<Environment>();

	// Methods for changing and accessing the program environment

	public static void newScope() {
		// Create the new maps and add them on top of the current scope
		HashMap<String, Function> functionMap = new HashMap<String, Function>();
		HashMap<String, BigDecimal> variableMap = new HashMap<String, BigDecimal>();
		Environment temp = new Environment(functionMap, variableMap);
		programEnvrioment.push(temp);
	}

	public static void endScope() {
		// Pop the scope only if it's not the global scope
		if (programEnvrioment.size() > 1) {
			programEnvrioment.pop();
		}
	}

	public static BigDecimal getOrCreateVar(String id) {
		// Check if it's in the current scope
		if (programEnvrioment.getLast().variableMap.containsKey(id)) {
			return programEnvrioment.getLast().variableMap.get(id);

		// Check if it's in the global scope
		} else if (programEnvrioment.getFirst().variableMap.containsKey(id)) {
			return programEnvrioment.getFirst().variableMap.get(id);

		// If neither, add it to the current scope as 0
		} else {
			programEnvrioment.getLast().variableMap.put(id, BigDecimal.ZERO);
			return BigDecimal.ZERO;
		}
	}

	public static void setVar(String id, BigDecimal value) {
		// Handle modifying scale (can't be negative)
		if (id.equals("scale") && value.compareTo(BigDecimal.ZERO) == -1) {
			System.out.println("Cannot set scale to negative value");
			throw new RuntimeException("Function undefined");
		}

		// Add the variable to the current scope
		programEnvrioment.getLast().variableMap.put(id, value);
	}

	public static Function getFunc(String id) {
		// Check if it's in the current scope
		if (programEnvrioment.getLast().functionMap.containsKey(id)) {
			return programEnvrioment.getLast().functionMap.get(id);

		// Check if it's in the global scope
		} else if (programEnvrioment.getFirst().functionMap.containsKey(id)) {
			return programEnvrioment.getFirst().functionMap.get(id);

		// If neither, there is an error
		} else {
			System.out.println("Function undefined");
			throw new RuntimeException("Function undefined");
		}
	}

	public static void setFunc(/* String id, BigDecimal value */) {
		// Handle modifying scale (always global)
		/* if (id.equals("scale")) {
			if (value.compareTo(BigDecimal.ZERO) == -1) {
				System.out.println("Cannot set scale to negative value");
				System.exit(-1);
			}
			scale = value.intValue();
		}

		// Add the variable to the current scope
		programEnvrioment.getLast().variableMap.put(id, value); */
	}

	// Create the global scope
	static {
		// Global function and variable map
		HashMap<String, Function> globalFunctionMap = new HashMap<String, Function>();
		HashMap<String, BigDecimal> globalVariableMap = new HashMap<String, BigDecimal>();

		// Functions
		globalFunctionMap.put("sqrt", new Function() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(Math.sqrt(arg.doubleValue())); } });
		globalFunctionMap.put("s", new Function() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(Math.sin(arg.doubleValue())); } });
		globalFunctionMap.put("c", new Function() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(Math.cos(arg.doubleValue())); } });
		globalFunctionMap.put("l", new Function() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(Math.log(arg.doubleValue())); } });
		globalFunctionMap.put("e", new Function() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(Math.exp(arg.doubleValue())); } });
		globalFunctionMap.put("read", new Function() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(input.nextLine().trim()); } });

		// Variables
		globalVariableMap.put("last", BigDecimal.ZERO);
		globalVariableMap.put("scale", new BigDecimal(20));

		// Add global scope to stack
		programEnvrioment.push(new Environment(globalFunctionMap, globalVariableMap));
	}

	/* Visitors */

	// Statements (modify state or do something)
	@Override
	public BigDecimal visitVarStat(SimpleBCParser.VarStatContext ctx) {
		String var = ctx.varDef().ID().getText();
		BigDecimal value = visit(ctx.varDef().expr());
		setVar(var, value);
		return value;
	}

	@Override
	public BigDecimal visitExprStat(SimpleBCParser.ExprStatContext ctx) {
		BigDecimal result = visit(ctx.expr());
		setVar("last", result);
		System.out.println(result);
		return result;
	}

	// Print Statements
	@Override
	public BigDecimal visitFuncPrint(SimpleBCParser.FuncPrintContext ctx) {
		// The print function should all the arguments on the same line before returning
		BigDecimal result = visitChildren(ctx);
		System.out.println();
		return result;
	}

	@Override
	public BigDecimal visitStringPrint(SimpleBCParser.StringPrintContext ctx) {
		System.out.println(ctx.ID().getText());
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal visitExprPrintEval(SimpleBCParser.ExprPrintEvalContext ctx) {
		System.out.print(visit(ctx.expr()));
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal visitStringPrintEval(SimpleBCParser.StringPrintEvalContext ctx) {
		System.out.print(ctx.ID().getText());
		return BigDecimal.ZERO;
	}

	// Expressions (return a value always)
	// Float
	@Override
	public BigDecimal visitFloatExpr(SimpleBCParser.FloatExprContext ctx) {
		return new BigDecimal(ctx.FLOAT().getText());
	}

	// ID
	@Override
	public BigDecimal visitVarExpr(SimpleBCParser.VarExprContext ctx) {
		return getOrCreateVar(ctx.ID().getText());
	}

	// Function ()
	@Override
	public BigDecimal visitFuncCallExpr(SimpleBCParser.FuncCallExprContext ctx) {
		String funcName = ctx.funcCall().ID().getText();

		// Make the new scope
		BigDecimal arg = visit(ctx.funcCall().parameters());
		return getFunc(funcName).execute(arg).setScale(getOrCreateVar("scale").intValueExact(), BigDecimal.ROUND_DOWN);
	}

	// if ( expression ) statement1 [else statement2]
	@Override
	public BigDecimal visitIfThen(SimpleBCParser.IfThenContext ctx) {
		// Check if the expression is truth-y
		if (0 != BigDecimal.ZERO.compareTo(visit(ctx.expr()))) {
			return visit(ctx.stat(0));
		} else if (ctx.stat().size() > 1){
			// The expression is false and there is an else block
			return visit(ctx.stat(1));
		} else {
			// No else block
			return BigDecimal.ZERO;
		}
	}

	//  while ( expression ) statement
	@Override
	public BigDecimal visitWhileLoop(SimpleBCParser.WhileLoopContext ctx) {
		// Check of the statement is truth-y
		while(0 != BigDecimal.ZERO.compareTo(visit(ctx.expr()))) {
			// Run the block
			visit(ctx.stat());
		}
		return BigDecimal.ZERO;
	}

	// for ( [expression1] ; [expression2] ; [expression3] ) statement
	@Override
	public BigDecimal visitForLoop(SimpleBCParser.ForLoopContext ctx) {
		// Run the for loop expressions
		for (
			visit(ctx.expr(0));
			0 != BigDecimal.ZERO.compareTo(visit(ctx.expr(1)));
			visit(ctx.expr(2))) {
				// Run the block
				visit(ctx.stat());
		}
		return BigDecimal.ZERO;
	}

	// define name ( parameters ) { newline auto_list statement_list }
	@Override
	public BigDecimal visitFuncStat(SimpleBCParser.FuncStatContext ctx) {
		return visitChildren(ctx);
	}

	// '++' ID, '--' ID
	@Override
	public BigDecimal visitPreUnExpr(SimpleBCParser.PreUnExprContext ctx) {
		String var = ctx.ID().getText();
		BigDecimal originalValue = getOrCreateVar(var);
		switch (ctx.op.getText()) {
			case "++":
				setVar(var, originalValue.add(BigDecimal.ONE));
				return originalValue.add(BigDecimal.ONE);
			case "--":
				setVar(var, originalValue.subtract(BigDecimal.ONE));
				return originalValue.subtract(BigDecimal.ONE);
			default:
				return BigDecimal.ZERO;
		}
	}

	// '-' expr, ! expr
	@Override public BigDecimal visitUnExpr(SimpleBCParser.UnExprContext ctx) {
		BigDecimal value = visit(ctx.expr());
		switch (ctx.op.getText()) {
			case "-":
				return value.negate();
			case "!":
				if (value.equals(BigDecimal.ZERO)) {
					return BigDecimal.ONE;
				} else {
					return BigDecimal.ZERO;
				}
			default:
				return BigDecimal.ZERO;
		}
	}

	// ID '++', ID '--'
	@Override
	public BigDecimal visitPostUnExpr(SimpleBCParser.PostUnExprContext ctx) {
		String var = ctx.ID().getText();
		BigDecimal originalValue = getOrCreateVar(var);
		switch (ctx.op.getText()) {
			case "++":
				setVar(var, originalValue.add(BigDecimal.ONE));
				return originalValue;
			case "--":
				setVar(var, originalValue.subtract(BigDecimal.ONE));
				return originalValue;
			default:
				return BigDecimal.ZERO;
		}
	}

	// ID '=' expr
	@Override
	public BigDecimal visitVarDefExpr(SimpleBCParser.VarDefExprContext ctx) {
		String var = ctx.varDef().ID().getText();
		BigDecimal value = visit(ctx.varDef().expr());
		setVar(var, value);
		return value;
	}

	// '(' ... ')'
	@Override
	public BigDecimal visitParenExpr(SimpleBCParser.ParenExprContext ctx) {
		return visit(ctx.expr());
	}

	// '^', '*', '/', '+', '-', '<', '<=', '>', '>=', '==', '!=', '&&', '||'
	@Override
	public BigDecimal visitBiExpr(SimpleBCParser.BiExprContext ctx) {
		BigDecimal left = visit(ctx.el);
		BigDecimal right = visit(ctx.er);
		switch (ctx.op.getText()) {
			case "^":
				return left.pow(right.intValue());
			case "*":
				return left.multiply(right);
			case "/":
				return left.divide(right, getOrCreateVar("scale").intValueExact(), BigDecimal.ROUND_DOWN);
			case "+":
				return left.add(right);
			case "-":
				return left.subtract(right);
			case "<":
				if (-1 == left.compareTo(right)) {
					return BigDecimal.ONE;
				} else {
					return BigDecimal.ZERO;
				}
			case "<=":
				if (-1 == left.compareTo(right) || 0 == left.compareTo(right)) {
					return BigDecimal.ONE;
				} else {
					return BigDecimal.ZERO;
				}
			case ">":
				if (1 == left.compareTo(right)) {
					return BigDecimal.ONE;
				} else {
					return BigDecimal.ZERO;
				}
			case ">=":
				if (1 == left.compareTo(right) || 0 == left.compareTo(right)) {
					return BigDecimal.ONE;
				} else {
					return BigDecimal.ZERO;
				}
			case "==":
				if (0 == left.compareTo(right)) {
					return BigDecimal.ONE;
				} else {
					return BigDecimal.ZERO;
				}
			case "!=":
				if (0 != left.compareTo(right)) {
					return BigDecimal.ONE;
				} else {
					return BigDecimal.ZERO;
				}
			case "&&":
				if (!left.equals(BigDecimal.ZERO) && !right.equals(BigDecimal.ZERO)) {
					return BigDecimal.ONE;
				} else {
					return BigDecimal.ZERO;
				}
			case "||":
				if (!left.equals(BigDecimal.ZERO) || !right.equals(BigDecimal.ZERO)) {
					return BigDecimal.ONE;
				} else {
					return BigDecimal.ZERO;
				}
			default:
				return BigDecimal.ZERO;
		}
	}
}

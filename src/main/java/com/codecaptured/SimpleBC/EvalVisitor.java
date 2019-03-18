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

	// Create function class
	public class Function {
		public SimpleBCParser.ParametersContext parameters;
		public SimpleBCParser.StatListContext body;
		public SimpleBCParser.AutoListContext autoVariables;

		public Function(SimpleBCParser.ParametersContext parameters, SimpleBCParser.StatListContext body, SimpleBCParser.AutoListContext autoVariables) {
			this.parameters = parameters;
			this.body = body;
			this.autoVariables = autoVariables;
		}

		public BigDecimal execute(SimpleBCParser.ArgumentsContext args) {
			// New scope
			newScope();

			// Add any arguments to the new scope
			for (int i = 0; i < parameters.ID().size(); ++i){
				setVar(parameters.ID(i).toString(), visit(args.expr(i)));
			}

			// Add scope specific variables
			if (null != autoVariables) {
				for (int i = 0; i < autoVariables.ID().size(); ++i) {
					setVar(autoVariables.ID(i).toString(), BigDecimal.ZERO);
				}
			}

			// Run the function
			BigDecimal result = BigDecimal.ZERO;
			if (body != null) {
				visit(body);
			}

			if (programEnvrioment.getLast().isReturn != null) {
				result = programEnvrioment.getLast().isReturn;
				programEnvrioment.getLast().isReturn = null;
			}

			String test = result.toString();

			// Remove the new scope
			endScope();

			// Return value
			return result.setScale(getOrCreateVar("scale").intValueExact(), BigDecimal.ROUND_DOWN);
		}
	}

	// Create environment
	public static class Environment {
		public HashMap<String, Function> functionMap;
		public HashMap<String, BigDecimal> variableMap;

		public BigDecimal isReturn = null;
		public boolean isContinue = false;
		public boolean isBreak = false;

		public Environment(HashMap<String, Function> functionMap, HashMap<String, BigDecimal> variableMap) {
			this.functionMap = functionMap;
			this.variableMap = variableMap;
		}
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
		// Check if it's a default

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

	public static void setFunc(String id, Function function) {
		// Add the function to the current scope
		programEnvrioment.getLast().functionMap.put(id, function);
	}

	// Create the global scope
	static {
		// Global function and variable map
		HashMap<String, Function> globalFunctionMap = new HashMap<String, Function>();
		HashMap<String, BigDecimal> globalVariableMap = new HashMap<String, BigDecimal>();

		// Add global scope to stack
		programEnvrioment.push(new Environment(globalFunctionMap, globalVariableMap));

		// Set default variables
		setVar("last", BigDecimal.ZERO);
		setVar("scale", new BigDecimal(20));
	}

	/* Visitors */

	// Statements (modify state or do something)
	@Override
	public BigDecimal visitStatList(SimpleBCParser.StatListContext ctx) {
		BigDecimal result = visitChildren(ctx);

		/* if (result == null) {
			System.out.println("Stat list result is null");
		} else {
			System.out.println("Stat list result is not null");
		} */

		if (result == null) {
			result = BigDecimal.ZERO;
		}
		String test = result.toString();
		return result;
	}

	@Override
	public BigDecimal visitVarStat(SimpleBCParser.VarStatContext ctx) {
		if (programEnvrioment.getLast().isBreak || programEnvrioment.getLast().isContinue || programEnvrioment.getLast().isReturn != null) {
			return BigDecimal.ZERO;
		} else {
			String var = ctx.varDef().ID().getText();
			BigDecimal value = visit(ctx.varDef().expr());
			setVar(var, value);
			return value;
		}
	}

	@Override
	public BigDecimal visitExprStat(SimpleBCParser.ExprStatContext ctx) {
		if (programEnvrioment.getLast().isBreak || programEnvrioment.getLast().isContinue || programEnvrioment.getLast().isReturn != null) {
			return BigDecimal.ZERO;
		} else {
			BigDecimal result = visit(ctx.expr());
			setVar("last", result);
			System.out.println(result);
			return result;
		}
	}

	// return
	@Override
	public BigDecimal visitReturnEmpty(SimpleBCParser.ReturnEmptyContext ctx) {
		if (programEnvrioment.getLast().isBreak || programEnvrioment.getLast().isContinue) {
			return BigDecimal.ZERO;
		} else {
			programEnvrioment.getLast().isReturn = BigDecimal.ZERO;
			return BigDecimal.ZERO;
		}
	}

	// return (expr)
	@Override
	public BigDecimal visitReturnValue(SimpleBCParser.ReturnValueContext ctx) {
		if (programEnvrioment.getLast().isBreak || programEnvrioment.getLast().isContinue) {
			return BigDecimal.ZERO;
		} else {
			BigDecimal debugResult = visit(ctx.expr());
			programEnvrioment.getLast().isReturn = debugResult;
			String test = debugResult.toString();
			return debugResult;
		}
	}

	// continue
	@Override
	public BigDecimal visitContine(SimpleBCParser.ContineContext ctx) {
		programEnvrioment.getLast().isContinue = true;
		return visitChildren(ctx);
	}

	// break
	@Override
	public BigDecimal visitBrek(SimpleBCParser.BrekContext ctx) {
		programEnvrioment.getLast().isBreak = true;
		return visitChildren(ctx);
	}

	// Print Statements
	@Override
	public BigDecimal visitFuncPrint(SimpleBCParser.FuncPrintContext ctx) {
		// The print function should all the arguments on the same line before returning
		if (programEnvrioment.getLast().isBreak || programEnvrioment.getLast().isContinue || programEnvrioment.getLast().isReturn != null) {
			return BigDecimal.ZERO;
		} else {
			BigDecimal result = visitChildren(ctx);
			System.out.println();
			return result;
		}
	}

	@Override
	public BigDecimal visitStringPrint(SimpleBCParser.StringPrintContext ctx) {
		if (programEnvrioment.getLast().isBreak || programEnvrioment.getLast().isContinue || programEnvrioment.getLast().isReturn != null) {
			return BigDecimal.ZERO;
		} else {
			System.out.println(ctx.ID().getText());
			return BigDecimal.ZERO;
		}
	}

	@Override
	public BigDecimal visitExprPrintEval(SimpleBCParser.ExprPrintEvalContext ctx) {
		if (programEnvrioment.getLast().isBreak || programEnvrioment.getLast().isContinue || programEnvrioment.getLast().isReturn != null) {
			return BigDecimal.ZERO;
		} else {
			System.out.print(visit(ctx.expr()));
			return BigDecimal.ZERO;
		}
	}

	@Override
	public BigDecimal visitStringPrintEval(SimpleBCParser.StringPrintEvalContext ctx) {
		if (programEnvrioment.getLast().isBreak || programEnvrioment.getLast().isContinue || programEnvrioment.getLast().isReturn != null) {
			return BigDecimal.ZERO;
		} else {
			System.out.print(ctx.ID().getText());
			return BigDecimal.ZERO;
		}
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

	// function()
	@Override
	public BigDecimal visitFuncCall(SimpleBCParser.FuncCallContext ctx) {
		// Value for the default function calls, which will be used to easily scale the value below
		Double defaultValue = 0.0;

		// Look through all of the default functions that aren't defined by the user (in bc)
		switch (ctx.ID().toString()) {
			case "sqrt":
				defaultValue = Math.sqrt(visit(ctx.arguments().expr(0)).doubleValue());
				break;
			case "s":
				defaultValue = Math.sin(visit(ctx.arguments().expr(0)).doubleValue());
				break;
			case "c":
				defaultValue = Math.cos(visit(ctx.arguments().expr(0)).doubleValue());
				break;
			case "l":
				defaultValue = Math.log(visit(ctx.arguments().expr(0)).doubleValue());
				break;
			case "e":
				defaultValue = Math.exp(visit(ctx.arguments().expr(0)).doubleValue());
				break;
			case "read":
				defaultValue = Double.parseDouble(input.nextLine().trim());
			// Now for user defined functions
			default:
				// System.out.println("Function call for: " + ctx.ID().getText());
				return getFunc(ctx.ID().getText()).execute(ctx.arguments());
		}

		// The defaults aren't scaled properly like the user function is in execute
		BigDecimal result = new BigDecimal(defaultValue);
		return result.setScale(getOrCreateVar("scale").intValueExact(), BigDecimal.ROUND_DOWN);
	}

	// if ( expression ) statement1 [else statement2]
	@Override
	public BigDecimal visitIfThen(SimpleBCParser.IfThenContext ctx) {
		// Check if the expression is truth-y
		if (0 != BigDecimal.ZERO.compareTo(visit(ctx.expr()))) {
			// System.out.println("I've hit true");
			return visit(ctx.stat(0));
		} else if (ctx.stat().size() > 1){
			// The expression is false and there is an else block
			// System.out.println("I've hit false");
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
			// Act on any breaks or continues
			if (programEnvrioment.getLast().isContinue) {
				programEnvrioment.getLast().isContinue = false;
				programEnvrioment.getLast().isBreak = false;
			} else if (programEnvrioment.getLast().isBreak) {
				programEnvrioment.getLast().isContinue = false;
				programEnvrioment.getLast().isBreak = false;
				return BigDecimal.ZERO;
			}
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
				// Act on any breaks or continues
				if (programEnvrioment.getLast().isContinue) {
					programEnvrioment.getLast().isContinue = false;
					programEnvrioment.getLast().isBreak = false;
				} else if (programEnvrioment.getLast().isBreak) {
					programEnvrioment.getLast().isContinue = false;
					programEnvrioment.getLast().isBreak = false;
					return BigDecimal.ZERO;
				}
		}
		return BigDecimal.ZERO;
	}

/* 	// define name ( parameters ) { newline auto_list statement_list }
	@Override
	public BigDecimal visitFuncStat(SimpleBCParser.FuncStatContext ctx) {
		return visitChildren(ctx);
	} */

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
		if (programEnvrioment.getLast().isBreak || programEnvrioment.getLast().isContinue || programEnvrioment.getLast().isReturn != null) {
			return BigDecimal.ZERO;
		} else {
			String var = ctx.varDef().ID().getText();
			BigDecimal value = visit(ctx.varDef().expr());
			setVar(var, value);
			return value;
		}
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
				// System.out.println("Multiplied to get: " + left.multiply(right));
				return left.multiply(right);
			case "/":
				return left.divide(right, getOrCreateVar("scale").intValueExact(), BigDecimal.ROUND_DOWN);
			case "+":
				return left.add(right);
			case "-":
				// System.out.println("Subtracted to get: " + left.subtract(right));
				return left.subtract(right);
			case "<":
				if (-1 == left.compareTo(right)) {
					return BigDecimal.ONE;
				} else {
					return BigDecimal.ZERO;
				}
			case "<=":
				if (-1 == left.compareTo(right) || 0 == left.compareTo(right)) {
					// System.out.println("Less than or equal");
					return BigDecimal.ONE;
				} else {
					// System.out.println("Not less than or equal");
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

	// define name ( parameters ) { newline auto_list statement_list }
	@Override
	public BigDecimal visitFuncDef(SimpleBCParser.FuncDefContext ctx) {
		Function newFunc = new Function(ctx.parameters(), ctx.statList(), ctx.autoList());
		setFunc(ctx.ID().getText(), newFunc);
		return BigDecimal.ZERO;
	}


}

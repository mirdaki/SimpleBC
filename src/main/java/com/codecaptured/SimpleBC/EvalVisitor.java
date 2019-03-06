package com.codecaptured.SimpleBC;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.math.BigDecimal;

public class EvalVisitor extends SimpleBCBaseVisitor<BigDecimal> {

	/* Utilities and environment */

	// Input for functions
	public static Scanner input = new Scanner(System.in);

	// Define function interface and map
	public interface Fn {
			public BigDecimal execute(BigDecimal arg);
	}

	public static HashMap<String, Fn> fnMap = new HashMap<String, Fn>();

	// Default functions
	static {
			fnMap.put("sqrt", new Fn() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(Math.sqrt(arg.doubleValue())); } });
			fnMap.put("s", new Fn() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(Math.sin(arg.doubleValue())); } });
			fnMap.put("c", new Fn() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(Math.cos(arg.doubleValue())); } });
			fnMap.put("l", new Fn() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(Math.log(arg.doubleValue())); } });
			fnMap.put("e", new Fn() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(Math.exp(arg.doubleValue())); } });
			fnMap.put("read", new Fn() { public BigDecimal execute(BigDecimal arg) { return new BigDecimal(input.nextLine().trim()); } });
	}

	// Variable map
	public static HashMap<String, BigDecimal> varMap = new HashMap<>();
	public static BigDecimal getOrCreate(String id) {
			if (id.equals("scale")) {
					return new BigDecimal(scale);
			}
			if (varMap.containsKey(id)) {
					return varMap.get(id);
			}
			else {
					varMap.put(id, BigDecimal.ZERO);
					return BigDecimal.ZERO;
			}
	}

	public static void set(String id, BigDecimal value) {
			//check that scale is not set to negative
			if (id.equals("scale")) {
					if (value.compareTo(BigDecimal.ZERO) == -1) {
							System.out.println("Cannot set scale to negative value");
							System.exit(-1);
					}
					scale = value.intValue();
			}
			varMap.put(id, value);

	}
	// Special variable
	static int scale = 20;

	// Default variables
	static {
			varMap.put("last", BigDecimal.ZERO);
	}

	/* Visitors */

	// Statements (modify state or do something)
	@Override
	public BigDecimal visitProg(SimpleBCParser.ProgContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public BigDecimal visitVarStat(SimpleBCParser.VarStatContext ctx) {
		String var = ctx.varDef().ID.getText();
		BigDecimal value = visit(ctx.varDef().expr);
		set(var, value);
		return value;
	}

	@Override public BigDecimal visitPrintFuncStat(SimpleBCParser.PrintFuncStatContext ctx) {
		/* {$i = "";} (( expr {$i += $expr.i;}
		| '"' s = ID '"' {$i += $s.text;}) ',')* (expr {varMap.put("last", $expr.i); $i += $expr.i; }
		| '"' s = ID '"' {$i += $s.text;}) */
		return BigDecimal.ZERO;
	}

	@Override public BigDecimal visitPrintQuoteStat(SimpleBCParser.PrintQuoteStatContext ctx) {
		System.out.println(ctx.printQuote().s.getText());
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal visitExprStat(SimpleBCParser.ExprStatContext ctx) {
		BigDecimal result = visit(ctx.expr());
		set("last", result);
		System.out.println(result);
		return result;
	}

	// Expressions (return a value always)
	// Float
	@Override
	public BigDecimal visitFloatExpr(SimpleBCParser.FloatExprContext ctx) {
		return new BigDecimal(ctx.FLOAT.getText());
	}

	// ID
	@Override
	public BigDecimal visitVarExpr(SimpleBCParser.VarExprContext ctx) {
		return getOrCreate(ctx.ID.getText());
	}

	// Function ()
	@Override
	public BigDecimal visitFuncExpr(SimpleBCParser.FuncExprContext ctx) {
		String funcName = ctx.func.ID.getText();
		BigDecimal arg = visit(ctx.func.arg);
		return fnMap.get(funcName).execute(arg).setScale(scale, BigDecimal.ROUND_DOWN);
	}

	// '++' ID, '--' ID
	@Override
	public BigDecimal visitPreUnExpr(SimpleBCParser.PreUnExprContext ctx) {
		String var = ctx.ID.getText();
		BigDecimal originalValue = getOrCreate(var);
		switch (ctx.op.getText()) {
			case "++":
				varMap.put(var, originalValue.add(BigDecimal.ONE));
				return originalValue.add(BigDecimal.ONE);
			case "--":
				varMap.put(var, originalValue.subtract(BigDecimal.ONE));
				return originalValue.subtract(BigDecimal.ONE);
			default:
				return BigDecimal.ZERO;
		}
	}

	// '-' expr, ! expr
	@Override public BigDecimal visitUnExpr(SimpleBCParser.UnExprContext ctx) {
		BigDecimal value = visit(ctx.e);
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
		String var = ctx.ID.getText();
		BigDecimal originalValue = getOrCreate(var);
		switch (ctx.op.getText()) {
			case "++":
				varMap.put(var, originalValue.add(BigDecimal.ONE));
				return originalValue;
			case "--":
				varMap.put(var, originalValue.subtract(BigDecimal.ONE));
				return originalValue;
			default:
				return BigDecimal.ZERO;
		}
	}

	// ID '=' expr
	@Override
	public BigDecimal visitVarDefExpr(SimpleBCParser.VarDefExprContext ctx) {
		String var = ctx.varDef.ID.getText();
		BigDecimal value = visit(ctx.varDef.expr);
		set(var, value);
		return value;
	}

	// '(' ... ')'
	@Override
	public BigDecimal visitParenExpr(SimpleBCParser.ParenExprContext ctx) {
		return visit(ctx.e);
	}

	// '^', '*', '/', '+', '-', '&&', '||'
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
				return left.divide(right);
			case "+":
				return left.add(right);
			case "-":
				return left.subtract(right);
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

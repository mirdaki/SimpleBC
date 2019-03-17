package com.codecaptured.SimpleBC;

import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.HashMap;

import com.codecaptured.SimpleBC.SimpleBCParser.FuncCallContext;

public class SimpleBCBaseVisitorPlus<T> extends SimpleBCBaseVisitor<T> {

	static HashMap<Integer, Integer> functionBodyMap = new HashMap<Integer, Integer>();
	static HashMap<Integer, Integer> returnMap = new HashMap<Integer, Integer>();

	static boolean wasFuncCall = false;

	@Override
	protected boolean shouldVisitNextChild(RuleNode node, T currentResult) {

		/* System.out.println("At " + node.hashCode() + " of: " + node.getRuleContext().toString() + ", " + node.getSourceInterval() + ", " + node.getChildCount());

		if (node.getRuleContext() instanceof SimpleBCParser.StatListContext) {
			System.out.println("StatListContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.RetrnContext) {
			System.out.println("RetrnContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.ReturnEmptyContext) {
			System.out.println("ReturnEmptyContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.ReturnStatContext) {
			System.out.println("ReturnStatContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.ReturnValueContext) {
			System.out.println("ReturnValueContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.FuncCallContext) {
			System.out.println("FuncCallContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.FuncCallExprContext) {
			System.out.println("FuncCallExprContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.FuncDefContext) {
			System.out.println("FuncDefContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.FuncPrintContext) {
			System.out.println("FuncPrintContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.FuncStatContext) {
			System.out.println("FuncStatContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.BlockContext) {
			System.out.println("BlockContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.BlockStatContext) {
			System.out.println("BlockStatContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.ExprContext) {
			System.out.println("ExprContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.VarDefContext) {
			System.out.println("VarDefContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.VarDefExprContext) {
			System.out.println("VarDefExprContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.VarExprContext) {
			System.out.println("VarExprContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.VarStatContext) {
			System.out.println("VarStatContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.BiExprContext) {
			System.out.println("BiExprContext");
		} else if (node.getRuleContext() instanceof SimpleBCParser.ParenExprContext) {
			System.out.println("ParenExprContext");
		} else {
			System.out.println("OTHER");
		}

		if (node.getParent() != null) {
			System.out.println("Parent: " + node.getParent().hashCode() + " has " + node.getParent().getText());
		} */

		// If funCall == ret, next fails
		if (node.getParent() != null) {
			int fu = functionBodyMap.getOrDefault(node.getParent().hashCode(), 0);
			int re = returnMap.getOrDefault(node.getParent().hashCode(), 0);
			// System.out.println("Don't continue on " + node.getParent().hashCode() + " if " + fu + " = " + re + " unless both are 0");

			if (fu == re && fu != 0) {
				return false;
			}
		}

		// First statlist in func call
		if (wasFuncCall) {
			wasFuncCall = false;
			int increment = functionBodyMap.getOrDefault(node.hashCode(), 0);
			functionBodyMap.put(node.hashCode(), ++increment);
			// System.out.println("Function Stack incremented to " + increment + " for " + node.hashCode());
		}

		// Return in func call
		if (node.getRuleContext() instanceof SimpleBCParser.ReturnStatContext) {
			int increment = returnMap.getOrDefault(node.getParent().hashCode(), 0);
			returnMap.put(node.getParent().hashCode(), ++increment);
			// System.out.println("Return Stack incremented to " + increment + " for " + node.getParent().hashCode());
		}

		// Mark there was a func call for the next round to use
		if (node.getRuleContext() instanceof SimpleBCParser.FuncCallExprContext) {
			wasFuncCall = true;
			// System.out.println("That was a function");
		}

		return true;
	}

	@Override
	public T visitChildren(RuleNode node) {
		T result = defaultResult();
		int n = node.getChildCount();
		for (int i=0; i<n; i++) {
			if (!shouldVisitNextChild(node, result)) {
				break;
			}

			ParseTree c = node.getChild(i);
			T childResult = c.accept(this);
			/* if (childResult != null) {
				String test = childResult.toString();
				System.out.println("Child result is: " + test);
			} else {
				System.out.println("Child result is NULL");
			} */
			result = aggregateResult(result, childResult);
		}

		return result;
	}

}

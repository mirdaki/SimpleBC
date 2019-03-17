package com.codecaptured.SimpleBC;

import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;

public class SimpleBCBaseVisitorPlus<T> extends SimpleBCBaseVisitor<T> {

	static boolean lastWasReturn = false;
	static HashMap<Integer, Boolean> returnMap = new HashMap<Integer, Boolean>();

	@Override
	protected boolean shouldVisitNextChild(RuleNode node, T currentResult) {
		// If the return has happened, reset and end function
		if (node.getRuleContext() instanceof SimpleBCParser.ProgContext != true && returnMap.containsKey(node.getParent().hashCode())) {
			/* if (returnMap.get(node.getPayload().hashCode()) == true) {
				returnMap.replace(node.getPayload().hashCode(), false);
				return true;
			} else {
				return false;
			} */
			return false;
		}

		if (node.getRuleContext() instanceof SimpleBCParser.ReturnStatContext) {
			returnMap.put(node.getParent().hashCode(), true);
		}

		/* if (lastWasReturn) {
			lastWasReturn = false;
			if (node.getParent() instanceof SimpleBCParser.ReturnValueContext) {
				node.hashCode();
				return true;
			} else {
				return false;
			}
		}

		// If it's a return, set the flag for next step
		if (node.getRuleContext() instanceof SimpleBCParser.ReturnStatContext) {
			lastWasReturn = true;
		} */

		return true;
	}

/* 	@Override
	public T visitChildren(RuleNode node) {
		T result = defaultResult();
		int n = node.getChildCount();
		for (int i=0; i<n; i++) {
			if (!shouldVisitNextChild(node, result)) {
				break;
			}

			ParseTree c = node.getChild(i);
			T childResult = c.accept(this);
			String test = childResult.toString();
			result = aggregateResult(result, childResult);
		}

		return result;
	} */

}

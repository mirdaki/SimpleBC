grammar SimpleBC;

/* Parser rules */
prog: (stat? EXPR_END)*;

stat
	: varDef #varStat
	| printFunc #printFuncStat
	| printQuote #printQuoteStat
	| expr #exprStat
	;

varDef
	: ID '=' expr
	;

/* printFunc returns[String i]
	: 'print' {$i = "";} (( expr {$i += $expr.i;}
		| '"' s = ID '"' {$i += $s.text;}) ',')* (expr {varMap.put("last", $expr.i); $i += $expr.i; }
		| '"' s = ID '"' {$i += $s.text;})
	; */

printFunc
	: 'print' (printEval ',')* printEval
	;

printEval
	: e = expr
	| '"' s = STRING '"'
	;

printQuote
	: '"' s = STRING '"'
	;

expr
	: op = ('++'|'--') ID #preUnExpr
	| ID op = ('++'|'--') #postUnExpr
	| op = '-' e = expr #unExpr
	| <assoc = right> el = expr op = '^' er = expr #biExpr
	| el = expr op = ('*'|'/') er = expr #biExpr
	| el = expr op = ('+'|'-') er = expr #biExpr
	| op = '!' e = expr #unExpr
	| el = expr op = '&&' er = expr #biExpr
	| el = expr op = '||' er = expr #biExpr
	| varDef #varDefExpr
	| FLOAT #floatExpr
	| ID #varExpr
	| func #funcExpr
	| '(' e = expr ')' #parenExpr
	;

func
	: ID '(' arg = expr ')'
	;

/* Lexer rules */
C_COMMENT: [/][*](. | [\r\n])*? [*][/] -> skip;
ID: [_A-Za-z]+;
FLOAT: [0-9]* [.]? [0-9]+;
STRING: .+;
EXPR_END: LINE_END | [;] | [EOF] | P_COMMENT;
WS: [ \t]+ -> skip;

fragment LINE_END: '\r'? '\n';
fragment P_COMMENT: [#](.)*? LINE_END;

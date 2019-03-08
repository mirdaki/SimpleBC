grammar SimpleBC;

/* Parser rules */
prog: (stat? EXPR_END)*;

stat
	: varDef #varStat
	| print #printStat
	| expr #exprStat
	;

varDef
	: ID '=' expr
	;

print
	: 'print' (printEval ',')* printEval #funcPrint
	| '"' ID '"' #stringPrint
	;

printEval
	: expr #exprPrintEval
	| '"' ID '"' #stringPrintEval
	;

expr
	: op = ('++'|'--') ID #preUnExpr
	| ID op = ('++'|'--') #postUnExpr
	| op = '-' expr #unExpr
	| <assoc = right> el = expr op = '^' er = expr #biExpr
	| el = expr op = ('*'|'/') er = expr #biExpr
	| el = expr op = ('+'|'-') er = expr #biExpr
	| op = '!' expr #unExpr
	| el = expr op = '&&' er = expr #biExpr
	| el = expr op = '||' er = expr #biExpr
	| varDef #varDefExpr
	| FLOAT #floatExpr
	| ID #varExpr
	| func #funcExpr
	| '(' expr ')' #parenExpr
	;

func
	: ID '(' arg = expr ')'
	;

/* Lexer rules */
C_COMMENT: [/][*](. | [\r\n])*? [*][/] -> skip;
ID: [_A-Za-z]+;
FLOAT: [0-9]* [.]? [0-9]+;
EXPR_END: LINE_END | [;] | [EOF] | P_COMMENT;
WS: [ \t]+ -> skip;

fragment LINE_END: '\r'? '\n';
fragment P_COMMENT: [#](.)*? LINE_END;

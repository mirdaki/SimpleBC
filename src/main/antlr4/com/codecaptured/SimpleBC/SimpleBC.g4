grammar SimpleBC;

/* Parser rules */
prog: (stat? EXPR_END)*;

stat
	: varDef #varStat
	| retrn #returnStat
	| print #printStat
	| expr #exprStat
	| ifThen #ifStat
	| whileLoop #whileStat
	| forLoop #forStat
	| funcDef #funcStat
	;

varDef
	: ID '=' expr
	;

retrn
	: 'return' #returnEmpty
	| 'return' '(' expr ')' #returnValue
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
	| funcCall #funcCallExpr
	| '(' expr ')' #parenExpr
	;

parameters
	: (ID ',')* ID #defineParameters
	| (expr ',')* expr #declareParameters
	;

funcCall
	: ID '(' parameters ')'
	;

block
	: '{' block '}'
	| stat
	;

ifThen
	: 'if' '(' expr ')' block ('else' block)?
	;

whileLoop
	: 'while' '(' expr ')' block
	;

forLoop
	: 'for' '(' expr ';' expr ';' expr ')' block
	;

funcDef
	: 'define' ID '(' parameters ')' '{' (autoList ';')? block '}'
	;

autoList
	: 'auto' (ID ',')* ID
	;

/* Lexer rules */
C_COMMENT: [/][*](. | [\r\n])*? [*][/] -> skip;
ID: [_A-Za-z]+;
FLOAT: [0-9]* [.]? [0-9]+;
EXPR_END: LINE_END | [;] | [EOF] | P_COMMENT;
WS: [ \t]+ -> skip;

fragment LINE_END: '\r'? '\n';
fragment P_COMMENT: [#](.)*? LINE_END;

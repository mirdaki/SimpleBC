grammar SimpleBC;

/* Parser rules */
prog
	: block EOF
	;

block
	: '{' block '}'
	// | (stat? EXPR_END)*
	| (stat? END_LINE)*
	;

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
	| el = expr op = ('<'|'<='|'>'|'>='|'=='|'!=') er = expr #biExpr
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
	: ((ID ',')* ID)? #defineParameters
	| ((expr ',')* expr)? #declareParameters
	;

funcCall
	: ID '(' parameters ')'
	;

ifThen
	: ('if' '(' expr ')' block) ('else' 'if' expr block)* ('else' block)?
	// | IF condition_block (ELSE IF condition_block)* (ELSE stat_block)?
	;

whileLoop
	: 'while' '(' expr ')' block
	;

forLoop
	: 'for' '(' expr ';' expr ';' expr ')' block
	;

// May need to remive the semicolon from auto list and add it to the list of statments (same with for stuff)
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
END_LINE: (SEMI_COLON NEW_LINE+) | (P_COMMENT NEW_LINE+) | NEW_LINE+;
// EXPR_END: P_COMMENT | SEMI_COLON | LINE_END | EOF;
WS: [ \t]+ -> skip;

fragment NEW_LINE: [\r\n] | [\n] | [\r];
fragment P_COMMENT: [#](.)*?;
fragment SEMI_COLON: [;];

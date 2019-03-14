grammar SimpleBC;

/* Parser rules */
prog
	: stat* EOF
	;

stat
	: END_LINE #endLineStat
	| block #blockStat
	| varDef END_LINE+ #varStat
	| retrn END_LINE+ #returnStat
	| print END_LINE+ #printStat
	| expr END_LINE+ #exprStat
	| ifThen #ifStat
	| whileLoop #whileStat
	| forLoop #forStat
	| funcDef END_LINE+ #funcStat
	;

block
	: '{' statList? '}'
	;

statList
	: stat+
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
	: 'if' '(' expr ')' stat ('else' stat)?
	// | IF condition_block (ELSE IF condition_block)* (ELSE stat_block)?
	;

whileLoop
	: 'while' '(' expr ')' stat
	;

forLoop
	: 'for' '(' expr ';' expr ';' expr ')' stat
	;

// May need to remive the semicolon from auto list and add it to the list of statments (same with for stuff)
funcDef
	: 'define' ID '(' parameters ')' '{' (autoList ';')? stat '}'
	;

autoList
	: 'auto' (ID ',')* ID
	;

/* Lexer rules */
P_COMMENT: [#] ~([\r\n])* -> skip;
C_COMMENT: [/][*](. | [\r\n])*? [*][/] -> skip;
WS: [ \t]+ -> skip;

END_LINE: (SEMI_COLON NEW_LINE*) | NEW_LINE+;
ID: [_A-Za-z]+;
FLOAT: [0-9]* [.]? [0-9]+;

// EXPR_END: P_COMMENT | SEMI_COLON | LINE_END | EOF;

fragment NEW_LINE: [\r\n] | [\n] | [\r];
fragment SEMI_COLON: [;];

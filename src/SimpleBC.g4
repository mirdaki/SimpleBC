grammar SimpleBC;

/* Include Java libraries */
@header{
	import java.util.HashMap;
	import java.util.Scanner;
}

/* Global Java code */
@members{
	// Input for functions
	public static Scanner input = new Scanner(System.in);

	// Define function interface and map
	public interface Fn {
		public double execute(double arg);
	}

	public static HashMap<String, Fn> fnMap = new HashMap<String, Fn>();

	static {
		fnMap.put("sqrt", new Fn() { public double execute(double arg) { return Math.sqrt(arg); } });
		fnMap.put("s", new Fn() { public double execute(double arg) { return Math.sin(arg); } });
		fnMap.put("c", new Fn() { public double execute(double arg) { return Math.cos(arg); } });
		fnMap.put("l", new Fn() { public double execute(double arg) { return Math.log(arg); } });
		fnMap.put("e", new Fn() { public double execute(double arg) { return Math.pow(Math.E, arg); } });
	}
}

/* Parser rules */
exprList: topExpr ( EXPR_END topExpr)* EXPR_END? ;

varDef: VAR ID '=' arith_expr;

topExpr:
		arith_expr { System.out.println(Double.toString($arith_expr.i)); }
	// | bool_expr { System.out.println(Double.toString($bool_expr.i)); }
	;

arith_expr returns [double i]:
		el=arith_expr op='^' er=arith_expr { $i=Math.pow($el.i, $er.i); }
	| el=arith_expr op='*' er=arith_expr { $i=$el.i*$er.i; }
	| el=arith_expr op='/' er=arith_expr { $i=$el.i/$er.i; }
	| el=arith_expr op='+' er=arith_expr { $i=$el.i+$er.i; }
	| el=arith_expr op='-' er=arith_expr { $i=$el.i-$er.i; }
	| el=arith_expr op='==' er=arith_expr { if ($el.i==$er.i) { $i=1; } else { $i=0; } }
	| op='!' e=arith_expr { if ($e.i==0) { $i=1; } else { $i=0; } }
	| el=arith_expr op='&&' er=arith_expr { if ($el.i!=0&&$er.i!=0) { $i=1; } else { $i=0; } }
	| el=arith_expr op='||' er=arith_expr { if ($el.i!=0||$er.i!=0) { $i=1; } else { $i=0; } }
	// | bool_expr { $i = $bool_expr.i; }
	| FLOAT { $i=Double.parseDouble($FLOAT.text); }
	| func { $i = $func.i ;}
	| ID
	| '(' e=arith_expr ')'
	;

/* bool_expr returns [double i]:
		el=arith_expr op='==' er=arith_expr {
			if ($el.i==$er.i) {
				$i=1;
			} else {
				$i=0;
			} }
	| FLOAT { $i=Double.parseDouble($FLOAT.text); }
	| func { $i = $func.i ;}
	| ID
	| '(' e=bool_expr ')'
	; */

func returns [double i]:
		'read()' { $i = input.nextDouble(); }
	| ID '(' arg=arith_expr ')' { $i=fnMap.get($ID.text).execute($arg.i); }
	;

/* Lexer rules */
COMMENT: [/][*](.)*?[*][/] -> skip;
/*
Comments is defined with the lazy definition so that
we match the nearest * /
*/

VAR: 'var';  // keyword
ID: [_A-Za-z]+;
FLOAT: [-]?[0-9]*[.]?[0-9]+;
EXPR_END: [(\r?\n);];
WS : [ \t]+ -> skip ;

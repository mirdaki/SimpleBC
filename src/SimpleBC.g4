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
	
	// Default functions
	static {
		fnMap.put("sqrt", new Fn() { public double execute(double arg) { return Math.sqrt(arg); } });
		fnMap.put("s", new Fn() { public double execute(double arg) { return Math.sin(arg); } });
		fnMap.put("c", new Fn() { public double execute(double arg) { return Math.cos(arg); } });
		fnMap.put("l", new Fn() { public double execute(double arg) { return Math.log(arg); } });
		fnMap.put("e", new Fn() { public double execute(double arg) { return Math.pow(Math.E, arg); } });
	}
	
	// Variable map
	public static HashMap<String, Double> varMap = new HashMap<>();
	public static Double getOrCreate(String id) {
		if (varMap.containsKey(id)) {
			return varMap.get(id);
		} else {
			varMap.put(id, 0.0);
			return 0.0;
		}
	}
}

/* Parser rules */
exprList: topExpr ( EXPR_END topExpr)* EXPR_END? ;

/* value assignments in bc return the value
however, if you only assign the value,
the statement the result is not printed */
varDef returns [double i]: ID '=' value=arith_expr { varMap.put($ID.text, $value.i); $i=$value.i; } ;

topExpr:
      varDef { System.out.println(Double.toString($varDef.i));}
    | arith_expr { System.out.println(Double.toString($arith_expr.i));}
    ;

arith_expr returns [double i]:
			op='++' ID { double oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal+1); $i=oldVal+1; }
		|	op='--' ID { double oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal-1); $i=oldVal-1; }
		| ID op='++' { double oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal+1); $i=oldVal; }
		| ID op='--' { double oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal-1); $i=oldVal; }
    | el=arith_expr op='^' er=arith_expr { $i=Math.pow($el.i, $er.i); }
    | el=arith_expr op='*' er=arith_expr { $i=$el.i*$er.i; }
    | el=arith_expr op='/' er=arith_expr { $i=$el.i/$er.i; }
    | el=arith_expr op='%' er=arith_expr { $i=$el.i%$er.i; }
    | el=arith_expr op='+' er=arith_expr { $i=$el.i+$er.i; }
    | el=arith_expr op='-' er=arith_expr { $i=$el.i-$er.i; }
		| op='!' e=arith_expr { if ($e.i==0) { $i=1; } else { $i=0; } }
		| el=arith_expr op='&&' er=arith_expr { if ($el.i!=0&&$er.i!=0) { $i=1; } else { $i=0; } }
		| el=arith_expr op='||' er=arith_expr { if ($el.i!=0||$er.i!=0) { $i=1; } else { $i=0; } }
    | var=varDef { $i=$var.i;}
    | FLOAT { $i=Double.parseDouble($FLOAT.text); }
    | ID { $i=getOrCreate($ID.text); }
    | func { $i = $func.i ;}
    | '(' e=arith_expr ')' { $i = $e.i; }
    ;

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

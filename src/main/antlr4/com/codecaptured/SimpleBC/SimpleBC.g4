grammar SimpleBC;

/* Include Java libraries */
@header {
    import java.util.HashMap;
    import java.util.Scanner;
    import java.math.BigDecimal;
}

/* Global Java code */
@members {
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
    // Defualt variables
    static {
        varMap.put("last", BigDecimal.ZERO);
    }
}

/* Parser rules */
prog: (stat? EXPR_END)*;

stat
	: varDef #varStat
	| printFunc { System.out.println($printFunc.i); } #printFuncStat
	| printQuote { System.out.println($printQuote.i); } #printQuoteStat
	| expr { set("last", $expr.i); /* System.out.println($expr.i); */ } #exprStat
	;

varDef returns[BigDecimal i]
	: ID '=' expr { set($ID.text, $expr.i); $i=$expr.i; };

printFunc returns[String i]
	: 'print' {$i = "";} (( expr {$i += $expr.i;}
		| '"' s = ID '"' {$i += $s.text;}) ',')* (expr {varMap.put("last", $expr.i); $i += $expr.i; }
		| '"' s = ID '"' {$i += $s.text;})
	;

printQuote returns[String i]
	: '"' s = STRING_INPUT '"' {$i += $s.text;}
	;

expr returns[BigDecimal i]
	: op = ('++'|'--') ID { BigDecimal oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal.add(BigDecimal.ONE)); $i=oldVal.add(BigDecimal.ONE); } #preUnExpr
	| op = '--' ID { BigDecimal oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal.subtract(BigDecimal.ONE)); $i=oldVal.subtract(BigDecimal.ONE); } #preUnExpr
	| ID op = ('++'|'--') { BigDecimal oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal.add(BigDecimal.ONE)); $i=oldVal; } #postUnExpr
	| ID op = '--' { BigDecimal oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal.subtract(BigDecimal.ONE)); $i=oldVal; } #postUnExpr
	| op = '-' e = expr { $i= $e.i.negate(); } #unExpr
	| <assoc = right> el = expr op = '^' er = expr { $i=($el.i.pow($er.i.intValue())); } #biExpr
	// note that floating point values cannot be passed to pow... just like bc
	| el = expr op = ('*'|'/') er = expr { $i=($op.text.equals("*")) ? $el.i.multiply($er.i) : $el.i.divide($er.i, scale, BigDecimal.ROUND_DOWN); } #biExpr
	| el = expr op = ('+'|'-') er = expr { $i=($op.text.equals("+")) ? $el.i.add($er.i) : $el.i.subtract($er.i); } #biExpr
	| op = '!' e = expr { if ($e.i.equals(BigDecimal.ZERO)) { $i=BigDecimal.ONE; } else { $i=BigDecimal.ZERO; } } #unExpr
	| el = expr op = '&&' er = expr { if (!($el.i.equals(BigDecimal.ZERO))&&!($er.i.equals(BigDecimal.ZERO))) { $i=BigDecimal.ONE; } else { $i=BigDecimal.ZERO; } } #biExpr
	| el = expr op = '||' er = expr { if (!($el.i.equals(BigDecimal.ZERO))||!($er.i.equals(BigDecimal.ZERO))) { $i=BigDecimal.ONE; } else { $i=BigDecimal.ZERO; } } #biExpr
	| varDef { $i = $varDef.i;} #varDefExpr
	| FLOAT { $i = new BigDecimal($FLOAT.text); } #floatExpr
	| ID { $i=getOrCreate($ID.text); } #varExpr
	| func { $i = $func.i ;} #funcExpr
	| '(' e = expr ')' { $i = $e.i; } #parenExpr
	;

func returns[BigDecimal i]
	: ID '(' arg = expr ')' { $i=fnMap.get($ID.text).execute($arg.i).setScale(scale, BigDecimal.ROUND_DOWN); }
	;

/* Lexer rules */
C_COMMENT: [/][*](. | [\r\n])*? [*][/] -> skip;
ID: [_A-Za-z]+;
FLOAT: [0-9]* [.]? [0-9]+;
STRING_INPUT: [_A-Za-z0-9.]+;
EXPR_END: LINE_END | [;] | [EOF] | P_COMMENT;
WS: [ \t]+ -> skip;

fragment LINE_END: '\r'? '\n';
fragment P_COMMENT: [#](.)*? LINE_END;

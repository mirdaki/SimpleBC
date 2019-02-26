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
exprList: (topExpr? EXPR_END)*;

/* value assignments in bc return the value however, if you only assign the value, the statement the
 * esult is not printed
 */
varDef
	returns[BigDecimal i]:
	ID '=' arithExpr { set($ID.text, $arithExpr.i); $i=$arithExpr.i; };

topExpr:
	varDef
	| printStatment {System.out.println($printStatment.i); }
	| arithExpr { set("last", $arithExpr.i); System.out.println($arithExpr.i); };

printStatment
	returns[String i]:
	'print' {$i = "";} (
		(
			arithExpr {$i += $arithExpr.i;}
			| '"' s = ID '"' {$i += $s.text;}
		) ','
	)* (
		arithExpr {varMap.put("last", $arithExpr.i); $i += $arithExpr.i;}
		| '"' s = ID '"' {$i += $s.text;}
	);

arithExpr
	returns[BigDecimal i]:
	op = '++' ID { BigDecimal oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal.add(BigDecimal.ONE)); $i=oldVal.add(BigDecimal.ONE);
		}
	| op = '--' ID { BigDecimal oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal.subtract(BigDecimal.ONE)); $i=oldVal.subtract(BigDecimal.ONE);
		}
	| ID op = '++' { BigDecimal oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal.add(BigDecimal.ONE)); $i=oldVal;
	}
	| ID op = '--' { BigDecimal oldVal = getOrCreate($ID.text); varMap.put($ID.text, oldVal.subtract(BigDecimal.ONE)); $i=oldVal;
		}
	| op = '-' e = arithExpr { $i= $e.i.negate(); }
	| <assoc = right> el = arithExpr op = '^' er = arithExpr { $i=($el.i.pow($er.i.intValue())); }
	// note that floating point values cannot be passed to pow... just like bc
	| el = arithExpr op = ('*' | '/') er = arithExpr { $i=($op.text.equals("*")) ? $el.i.multiply($er.i) : $el.i.divide($er.i, scale, BigDecimal.ROUND_DOWN);
		}
	| el = arithExpr op = ('+' | '-') er = arithExpr { $i=($op.text.equals("+")) ? $el.i.add($er.i) : $el.i.subtract($er.i);
		}
	| op = '!' e = arithExpr { if ($e.i.equals(BigDecimal.ZERO)) { $i=BigDecimal.ONE; } else { $i=BigDecimal.ZERO; }
		}
	| el = arithExpr op = '&&' er = arithExpr { if (!($el.i.equals(BigDecimal.ZERO))&&!($er.i.equals(BigDecimal.ZERO))) { $i=BigDecimal.ONE; } else { $i=BigDecimal.ZERO; }
		}
	| el = arithExpr op = '||' er = arithExpr { if (!($el.i.equals(BigDecimal.ZERO))||!($er.i.equals(BigDecimal.ZERO))) { $i=BigDecimal.ONE; } else { $i=BigDecimal.ZERO; }
		}
	| varDef { $i = $varDef.i;}
	| FLOAT { $i = new BigDecimal($FLOAT.text); }
	| ID { $i=getOrCreate($ID.text); }
	| func { $i = $func.i ;}
	| '(' e = arithExpr ')' { $i = $e.i; };

func
	returns[BigDecimal i]:
	'read()' { $i = new BigDecimal(input.nextLine().trim()); }
	| ID '(' arg = arithExpr ')' { $i=fnMap.get($ID.text).execute($arg.i).setScale(scale, BigDecimal.ROUND_DOWN);
		};

/* Lexer rules */
C_COMMENT: [/][*](. | [\r\n])*? [*][/] -> skip;
/*
 Comments is defined with the lazy definition so that we match the nearest * /
 */

VAR: 'var'; // keyword
ID: [_A-Za-z]+;
FLOAT: [0-9]* [.]? [0-9]+;
EXPR_END: LINE_END | [;] | [EOF] | P_COMMENT;
WS: [ \t]+ -> skip;

fragment LINE_END: '\r'? '\n';
fragment P_COMMENT: [#](.)*? LINE_END;

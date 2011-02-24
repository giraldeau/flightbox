/**
 * 
 */
package org.lttng.flightbox.automaton;

import java.util.List;
import java.util.Map;

import org.jdom.Element;

/**
 * @author francis
 *
 */
public class RegExp {

	Symbol symbol;
	RegExp exp1, exp2, exp;
	
	Kind kind;
	enum Kind {
		REGEXP_UNION,
		REGEXP_CONCAT,
		REGEXP_KLEEN,
		REGEXP_ONEORMORE,
		REGEXP_OPTIONAL,
		REGEXP_EMPTY,
		REGEXP_SINGLE
	}
	
	public RegExp() {
		kind = Kind.REGEXP_EMPTY;
	}
	
	public static RegExp makeSingle(Symbol q0) {
		if (q0==null){
			throw new NullPointerException("symbol can't be null");
		}
		RegExp re = new RegExp();
		re.symbol = q0;
		re.kind = Kind.REGEXP_SINGLE;
		return re;
	}

	public static RegExp makeConcat(RegExp r1, RegExp r2) {
		if (r1==null || r2 == null){
			throw new NullPointerException("r1 and r2 can't be null");
		}
		RegExp re = new RegExp();
		re.exp1 = r1;
		re.exp2 = r2;
		re.kind = Kind.REGEXP_CONCAT;
		return re;
	}

	public static RegExp makeUnion(RegExp r1, RegExp r2) {
		if (r1==null || r2 == null){
			throw new NullPointerException("r1 and r2 can't be null");
		}
		RegExp re = new RegExp();
		re.exp1 = r1;
		re.exp2 = r2;
		re.kind = Kind.REGEXP_UNION;
		return re;
	}
	
	public static RegExp makeEmpty() {
		return new RegExp();
	}

	
	public static RegExp makeKleen(RegExp r1){
		if (r1==null){
			throw new NullPointerException("regexp can't be null");
		}
		RegExp re = new RegExp();
		re.kind = Kind.REGEXP_KLEEN;
		re.exp = r1;
		return re;
	}
	
	public static RegExp makeOneOrMore(RegExp r1){
		if (r1==null){
			throw new NullPointerException("regexp can't be null");
		}

		RegExp re = new RegExp();
		re.kind = Kind.REGEXP_ONEORMORE;
		re.exp = r1;
		return re;
	}
	
	public static RegExp makeOptional(RegExp r1){
		if (r1==null){
			throw new NullPointerException("regexp can't be null");
		}

		RegExp re = new RegExp();
		re.kind = Kind.REGEXP_OPTIONAL;
		re.exp = r1;
		return re;
	}

	
	public Automaton toAutomaton() throws Exception {
		// recursively descend and build the automaton bottom-up
		Automaton a = null;
		Automaton a1 = null;
		Automaton a2 = null;
		switch(kind){
		case REGEXP_EMPTY:
			a = new Automaton();
			break;
		case REGEXP_SINGLE:
			a = new Automaton(symbol);
			break;
		case REGEXP_KLEEN:
			a = exp.toAutomaton();
			a.makeKleen();
			break;
		case REGEXP_ONEORMORE:
			a = exp.toAutomaton();
			a.makeOneOrMore();
			break;
		case REGEXP_OPTIONAL:
			a = exp.toAutomaton();
			a.makeOptional();
			break;
		case REGEXP_CONCAT:
			a1 = exp1.toAutomaton();
			a2 = exp2.toAutomaton();
			a1.concat(a2);
			a = a1;
			break;
		case REGEXP_UNION:
			a1 = exp1.toAutomaton();
			a2 = exp2.toAutomaton();
			a1.union(a2);
			a = a1;
			break;			
		default:
			throw new Exception("Not supported regexp type");
		}
		return a;
	}
	
	public boolean match(List<Symbol> content) {
		// TODO get automaton and test if children symbols correspond to the RegExp
		
		boolean res = true;
		Automaton a = null;
		try {
			a = toAutomaton();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		a.determinize();
		
		State cur = a.getStart();
		for (Symbol sym: content){
			State next = cur.getTransition(sym);
			if (next==null){
				res = false;
				break;
			} else {
				cur = next;
			}
		}
		if (!cur.isAccept()){
			res = false;
		}
		return res;
	}
	
	public String toString(){
		String s = "";
		switch (kind){
		case REGEXP_CONCAT:
			s = exp1.toString() + "," + exp2.toString();
			break;
		case REGEXP_UNION:
			s = exp1.toString() + "|" + exp2.toString();
		case REGEXP_EMPTY:
			break;
		case REGEXP_KLEEN:
			s = "(" + exp.toString() + ")*";
			break;
		case REGEXP_ONEORMORE:
			s = "(" + exp.toString() + ")+";
			break;
		case REGEXP_OPTIONAL:
			s = "(" + exp.toString() + ")?";
			break;
		case REGEXP_SINGLE:
			s = symbol.toString();
			break;
		}
		return s;
	}
}

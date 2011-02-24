package org.lttng.flightbox.automaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * Representation of automaton that recognize hedge states
 * @author francis
 */

public class Automaton {
	
	/** Initial state of this automaton. */
	State start;
	State end;

	/** If true, then this automaton is deterministic */
	boolean deterministic;

	static Symbol eps = new Event("eps");
	
	/** 
	 * Constructs a new automaton that accepts the empty language.
	 */
	
	public static Symbol getEps(){ return eps; }
	
	public Automaton() {
		this(eps);
	}
	
	public Automaton(Symbol symbol) {
		start = new State();
		end = new State();
		start.addTransition(symbol, end);
		end.setAccept(true);
		deterministic = false;
	}
	
	public String toString(){
		StringBuilder str = new StringBuilder();
		Set<State> set = getStates();
		for (State x : set){
			for (Symbol sym: x.getTransitionSymbols()){
				// FIXME: may be different
				Event y = (Event) sym;
				for (State z : x.getTransitions(y)){
					str.append(x.getId() + " --" +  y.getLabel() + "--> " + z.getId() + "\n");
				}
			}
		}
		return str.toString();
	}

	public State getStart() {
		return start;
	}

	public void setStart(State start) {
		this.start = start;
	}
	public State getEnd() {
		return end;
	}

	public void setEnd(State end) {
		this.end = end;
	}

	public Set<State> getClosure(Set<State> set){
		Set<State> res = new HashSet<State>();
		for (State s : set){
			for (State x : getClosure(s)){
				if (!res.contains(x)){
					res.add(x);
				}
			}
		}
		return res;
	}
	
	public Set<State> getClosure(State s){
		Stack<State> stack = new Stack<State>();
		Set<State> visited = new HashSet<State>();
		stack.add(s);
		while(stack.size() > 0){
			State current = stack.pop();
			visited.add(current);
			Set<State> x = current.getTransitions(eps);
			for (State y : x){
				if (!visited.contains(y)){
					stack.push(y);
				}
			}
		}
		return visited;
	}
	
	public Set<State> move(Set<State> set, Symbol symbol){
		Set<State> res = new HashSet<State>();
		for (State s : set){
			for (State x : s.getTransitions(symbol)){
				if (!res.contains(x)){
					res.add(x);
				}
			}
		}
		return res;
	}
	
	public void determinize(){
		// map new states for the DFA with old NFA states set 
		Map<State, Set<State>> dfa_map = new HashMap<State, Set<State>>();
		Set<Symbol> alpha = getAlphabet();
		alpha.remove(eps);
		Stack<State> process = new Stack<State>();
		State new_start = new State();
		State new_end = null; 
		Set<State> closure_start = getClosure(start);
		new_start.setAccept(containsAcceptingState(closure_start));
		dfa_map.put(new_start, closure_start);
		process.push(new_start);
		State cur;
		Set<State> cur_set;
		while(process.size()>0){
			cur = process.pop();
			new_end = cur;
			cur_set = dfa_map.get(cur);
			for (Symbol sym: alpha){
				Set<State> next = move(cur_set, sym);
				Set<State> next_closure = getClosure(next);
				if (next_closure.size()==0){
					continue;
				}
				State new_state = findState(dfa_map, next_closure);
				if (new_state==null){
					new_state = new State();
					new_state.setAccept(containsAcceptingState(next_closure));
					dfa_map.put(new_state, next_closure);
					process.push(new_state);
				}
				cur.addTransition(sym, new_state);
			}
		}
		deterministic = true;
		start = new_start;
		// FIXME : may not be appropriate to append other states from this end state
		end = new_end;
	}
	
	private boolean containsAcceptingState(Set<State> set){
		for (State s: set){
			if (s.isAccept()){
				return true;
			}
		}
		return false;
	}
	
	private State findState(Map<State, Set<State>> map, Set<State> set){
		for (State s: map.keySet()){
			Set<State> x = map.get(s);
			if (set.containsAll(x) &&
					x.containsAll(set)){
				return s;
			}
		}
		return null;
	}
	
	public Set<Symbol> getAlphabet(){
		// get all symbols from all states
		Set<Symbol> res = new HashSet<Symbol>();
		Stack<State> stack = new Stack<State>();
		Set<State> visited = new HashSet<State>();
		stack.add(start);
		while(stack.size() > 0){
			State current = stack.pop();
			visited.add(current);
			Set<State> x = current.getReachableStates();
			for (State y : x){
				if (!visited.contains(y)){
					stack.push(y);
				}
			}
		}
		for (State s :visited){
			for (Symbol x : s.getTransitionSymbols()){
				if (!res.contains(x)){
					res.add(x);
				}
			}
		}
		return res;
	}
	
	/**
	 * Returns deterministic flag for this automaton.
	 * @return true if the automaton is definitely deterministic, false if the automaton
	 *         may be nondeterministic
	 */
	public boolean isDeterministic() {
		return deterministic;
	}
	
	public int getSize(){
		return this.getStates().size();
	}
	
	public int getSizeWithTransitions(){
		int size = this.getStates().size();
		for (State s: this.getStates()){
			size = size + s.getTransitionsCount();
		}
		return size;
	}
	
	/**
	 * Returns the set of states that are reachable from the initial state
	 * @return set of {@link State} objects
	 */
	public Set<State> getStates(){
		LinkedHashSet<State> visited = new LinkedHashSet<State>();
		LinkedList<State> worklist = new LinkedList<State>();
		worklist.add(start);
		visited.add(start);
		while (worklist.size() > 0){
			State s = worklist.removeFirst();
			for (State x : s.getReachableStates()){
				if (!visited.contains(x)){
					visited.add(x);
					worklist.add(x);
				}
			}
			
		}
		return visited;
	}
	
	public void concat(Automaton other){
		end.setAccept(false);
		end.addTransition(eps, other.getStart());
		end = other.getEnd();
	}

	public void union(Automaton other){
		end.setAccept(false);
		other.getEnd().setAccept(false);
		State x = new State();
		State y = new State();
		x.addTransition(eps, start);
		x.addTransition(eps, other.getStart());
		end.addTransition(eps, y);
		other.getEnd().addTransition(eps, y);
		start = x;
		end = y;
		end.setAccept(true);
	}

	public void makeKleen() {
		end.setAccept(false);
		State x = new State();
		State y = new State();
		x.addTransition(eps, y);
		end.addTransition(eps, start);
		x.addTransition(eps, start);
		end.addTransition(eps, y);
		start = x;
		end = y;
		end.setAccept(true);
	}
	
	public void makeOptional(){
		// A? => A|empty
		Automaton x = new Automaton();
		this.union(x);
	}

	public void makeOneOrMore(){
		State x = new State();
		State y = new State();
		x.addTransition(eps, start);
		end.addTransition(eps, y);
		end.addTransition(eps, x);
		start = x;
		end = y;
		end.setAccept(true);
	}

	public boolean checkdeterminist() {
		Set<State> states = getStates();
		for (State s: states){
			if (!s.isDeterministic()){
				return false;
			}
		}
		return true;
	}
}

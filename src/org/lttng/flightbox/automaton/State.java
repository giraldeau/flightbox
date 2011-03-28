package org.lttng.flightbox.automaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Representation of an automaton state
 * @author francis
 *
 */

public class State {

	/** unique id of this state */
	int id;

	/** static counter for this state */
	static int counter = 0;

	/** accepting state */
	boolean accept;

	String label;

	/** set of transitions to other states */
	//Set<Transition> transitions;
	HashMap<Symbol, HashSet<State>> transitions;

	/**
	 * Construct a new state, and sets the id to the next available
	 */
	public State(){
		this(counter++);
	}

	/**
	 * Construct a new state
	 * @param id id of the state
	 */
	public State(int id){
		this.id = id;
		clearTransitions();
	}

	public State(String label) {
		this();
		this.label = label;
	}

	/**
	 * Clear all transitions set
	 */
	public void clearTransitions(){
		transitions = new HashMap<Symbol, HashSet<State>>();
	}

	/**
	 * Add a transition to a state triggered by a symbol
	 * @param symbol
	 * @param state
	 */
	public void addTransition(Symbol symbol, State state){
		if (!transitions.containsKey(symbol)){
			HashSet<State> s = new HashSet<State>();
			transitions.put(symbol, s);
		}
		Set<State> s = transitions.get(symbol);
		s.add(state);
	}

	/**
	 * Remove a transition from the transitions set
	 * @param symbol
	 */
	public void removeTransition(Symbol symbol){
		transitions.remove(symbol);
	}

	/**
	 * Replace a src states to dst states in the transition set
	 * @param src state
	 * @param dst state
	 * @return number of states replaced
	 */
	public int remplaceTransitions(State src, State dst){
		int nb_replaced = 0;
		for (Set<State> s : transitions.values()){
			if (s.contains(src)){
				s.remove(src);
				s.add(dst);
				nb_replaced++;
			}
		}
		return nb_replaced;
	}

	/**
	 * Returns the transitions set
	 * @returns transition set
	 */

	public Set<State> getTransitions(Symbol symbol){
		Set<State> x = transitions.get(symbol);
		if (x == null){
			x = new HashSet<State>();
		}
		return x;
	}

	public State getTransition(Symbol symbol){
		if (!transitions.containsKey(symbol)){
			return null;
		}
		Set<State> s = transitions.get(symbol);
		return (State)s.toArray()[0];
	}

	public Set<Symbol> getTransitionSymbols(){
		return transitions.keySet();
	}

	public Set<State> getReachableStates(){
		HashSet<State> set = new HashSet<State>();
		for (Set<State> s : transitions.values()){
			set.addAll(s);
		}
		return set;
	}

	/**
	 * Set the accept property of this state
	 * @param accept
	 */
	public void setAccept(boolean accept){
		this.accept = accept;
	}

	/**
	 * Returns if this state is accepting
	 * @return accept
	 */
	public boolean isAccept(){
		return accept;
	}

	/**
	 * Returns the id of this state
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of this state
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	public void resetCounter(){
		counter = 0;
	}

	@Override
	public String toString(){
		if (label != null){
			return label;
		} else {
			return Integer.toString(id);
		}
	}

	@Override
	public boolean equals(Object obj){
		if (obj instanceof State){
			State s = (State)obj;
			return this.id == s.id;
		}
		return false;
	}

	@Override
	public int hashCode(){
		return this.id * 3;
	}

	public int getTransitionsCount() {
		int count = 0;
		for (Set<State> set : transitions.values()){
			count += set.size();
		}
		return count;
	}

	public boolean isDeterministic(){
		for (HashSet<State> set: transitions.values()){
			if (set.size()>1){
				return false;
			}
		}
		return true;
	}
}

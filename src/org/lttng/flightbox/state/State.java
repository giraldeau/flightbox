package org.lttng.flightbox.state;

import java.util.HashSet;
import java.util.Set;

public class State<T> {

	Set<Transition<T>> transitions;
	Integer id;
	Boolean accept;
	String label;
	
	static Integer next_id;

	public State() {
		resetTransitions();
		id = next_id++;
	}

	public void resetTransitions() {
		transitions = new HashSet<Transition<T>>();
	}
	
	public Set<Transition<T>> getTransitions() {
		return transitions;
	}
	
	public void addTransition(Transition<T> t) {
		transitions.add(t);
	}
	
	public void setAccept(Boolean b) {
		accept = b;
	}
	
	public Boolean isAccept() {
		return accept;
	}
	
	public State<T> step(T sym) {
		for (Transition<T> t: transitions)
				for (T s: t.getSymbols())
					if (s.equals(sym))
						return t.to;
		return null;
	}
	
	public boolean equals(Object o) {
		return super.equals(o);
	}
	
	public int hashCode() {
		return super.hashCode();
	}
}

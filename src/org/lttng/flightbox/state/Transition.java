package org.lttng.flightbox.state;

import java.util.HashSet;
import java.util.Set;

public class Transition<T> {

	Set<T> symbols;
	State<T> to;

	public Transition() {
		this(null, null);
	}
	
	public Transition(T symbol, State<T> to) {
		resetSymbols();
		addSymbol(symbol);
		this.to = to;
	}

	public void resetSymbols() {
		symbols = new HashSet<T>();
	}
	
	public void addSymbol(T symbol) {
		if (symbol != null)
			this.symbols.add(symbol);
	}

	public Set<T> getSymbols() {
		return this.symbols;
	}
}
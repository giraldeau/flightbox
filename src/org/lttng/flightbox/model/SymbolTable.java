package org.lttng.flightbox.model;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

	private final Map<Integer, String> symbols;

	public SymbolTable() {
		symbols = new HashMap<Integer, String>();
	}

	public void add(int id, String symbol) {
		symbols.put(id, symbol);
	}

	public String get(int id) {
		return symbols.get(id);
	}

	public boolean contains(int id) {
		return symbols.containsKey(id);
	}

	public Map<Integer, String> getMap() {
		return symbols;
	}

}

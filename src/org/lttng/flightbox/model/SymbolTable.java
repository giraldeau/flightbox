package org.lttng.flightbox.model;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

	/* FIXME: to validate with kernel/arch/x86/kernel/syscall_table_32.S */
	/*
	public final static int SYS_SOCKET 	= 41;
	public final static int SYS_CONNECT = 42;
	public final static int SYS_READ 	= 0;
	public final static int SYS_WRITE 	= 1;
	public final static int SYS_CLOSE 	= 3;
	public static final int SYS_OPEN = 5;
	*/

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

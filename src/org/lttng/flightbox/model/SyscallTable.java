package org.lttng.flightbox.model;

import java.util.HashMap;
import java.util.Map;

public class SyscallTable {

	private final Map<Integer, String> syscalls;

	public SyscallTable() {
		syscalls = new HashMap<Integer, String>();
	}

	public void addSyscall(int id, String name) {
		syscalls.put(id, name);
	}

	public String getSyscallName(int id) {
		return syscalls.get(id);
	}

	public Map<Integer, String> getSyscallsMap() {
		return syscalls;
	}
}

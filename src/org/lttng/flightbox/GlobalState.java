package org.lttng.flightbox;

public class GlobalState {
	public enum KernelMode {
		USER, SYSCALL, TRAP, IRQ, SOFTIRQ
	}
}

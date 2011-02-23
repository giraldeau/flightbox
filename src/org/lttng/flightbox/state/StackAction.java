package org.lttng.flightbox.state;

public class StackAction {

	public enum Type {
		PUSH,
		POP
	};
	
	String eventName;
	Type type;
	String stackSymbol;

	public StackAction(String eventName, String stackSymbol, Type type) {
		this.eventName = eventName;
		this.stackSymbol = stackSymbol;
		this.type = type;
	}
	
	public String toString() {
		return "(" + eventName + "," + type + "," + stackSymbol + ")";
	}
}

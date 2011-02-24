package org.lttng.flightbox.state;

import java.util.Collection;
import java.util.HashMap;

public class StackMachine {

	HashMap<String, StackAction> actions;
	private String stackName;
	
	public StackMachine() {
		actions = new HashMap<String, StackAction>();
	}
	
	public void addAction(StackAction action) {
		actions.put(action.eventName, action);
	}

	public void setName(String stackName) {
		this.stackName = stackName;
	}
	public String getName() {
		return this.stackName;
	}
	public String toString() {
		return "[" + stackName + ",size=" + actions.size() + "]";
	}

	public Collection<StackAction> getActions() {
		return actions.values();
	}

	public boolean step(VersionizedStack<String> stack, String eventName, Long eventTs) {
		/* get the required action, if any */
		StackAction action = actions.get(eventName);
		/* FIXME: the following code will work only for the type String */
		if (action == null)
			return true;
		if (action.type.equals(StackAction.Type.PUSH)) {
			stack.push(action.stackSymbol, eventTs);
		} else if (action.type.equals(StackAction.Type.POP)) {
			String top = stack.pop(eventTs);
			if (!top.equals(action.stackSymbol)) {
				return false;
			}
		}
		return true;
	}

 
}

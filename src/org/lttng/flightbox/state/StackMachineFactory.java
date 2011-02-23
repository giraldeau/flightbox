package org.lttng.flightbox.state;

import java.util.List;

import org.jdom.Element;
import org.lttng.flightbox.state.StackAction.Type;

public class StackMachineFactory {

	
	public static StackMachine fromXml(Element stackElement) {
		
		StackMachine stackMachine = new StackMachine();
		String stackName = stackElement.getAttributeValue("name");
		stackMachine.setName(stackName);
		List<Element> stateDef = stackElement.getChildren("action");
		for (Element def: stateDef) {
			String type = def.getAttributeValue("type");
			String name = def.getAttributeValue("name");
			String event = def.getAttributeValue("event");
			Type enumType = null;
			if (type.equals("push")) {
				enumType = StackAction.Type.PUSH;
			} else if (type.equals("pop")) {
				enumType = StackAction.Type.POP;
			}
			StackAction action = new StackAction(event, name, enumType);
			stackMachine.addAction(action);
		}
		return stackMachine;
	}
}

package org.lttng.flightbox;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.state.StackMachine;
import org.lttng.flightbox.state.StackMachineFactory;
import org.lttng.flightbox.state.TraceEventHandlerState;
import org.lttng.flightbox.state.VersionizedStack;
import org.lttng.flightbox.ui.StackView;
import org.lttng.flightbox.xml.ManifestReader;

public class MainStack {

	public static void main(String[] args) throws JniException, JDOMException, IOException {
		
		MainStack mainWindow = new MainStack();
		mainWindow.run(args[0]);
	}
	
	public void run(String trace) throws JDOMException, IOException, JniException {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Flightbox");
		shell.setSize(800, 800);
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		
		Map<String, StackMachine> machines;
		
		URL manifest = getClass().getResource("/manifest/mariadb.xml");
		
		ManifestReader reader = new ManifestReader();
		Document doc = reader.read(manifest);
		
		XPath xpath = XPath.newInstance("/manifest/stack");
		List<Element> res = (List<Element>) xpath.selectNodes(doc);
		
		machines = new HashMap<String, StackMachine>();
		
		for(Element elem: res) {
			StackMachine machine = StackMachineFactory.fromXml(elem);
			machines.put(machine.getName(), machine);
		}

		TraceReader reader2 = new TraceReader(trace);
		TraceEventHandlerState handler = new TraceEventHandlerState();
		handler.addAllStackMachine(machines);
		reader2.register(handler);
		reader2.process();
		Map<String, VersionizedStack<String>> objectState = handler.getObjectState();
		
		System.out.println(objectState);
		
		StackView view = new StackView(shell, SWT.NONE);
		//view.setTimeInterval(reader2.getStartTime(), reader2.getEndTime());
		view.setStack(objectState.get("default"));
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		shell.dispose();
	}
	
}

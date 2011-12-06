package org.lttng.flightbox;

import java.io.IOException;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jdom.JDOMException;
import org.lttng.flightbox.state.VersionizedStack;
import org.lttng.flightbox.ui.IntervalView;

public class MainStack {

	public static void main(String[] args) throws JniException, JDOMException, IOException {
		
		MainStack mainWindow = new MainStack();
		mainWindow.run(null);
	}
	
	public void run(String trace) throws JDOMException, IOException, JniException {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Flightbox");
		shell.setSize(800, 800);
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		
		IntervalView widget = new IntervalView(shell, SWT.NONE);
		VersionizedStack<String> stack = new VersionizedStack<String>();
		int i;
		int max = 1000;
		for (i=0; i<max; i++) {
			long disp = i * 100;
			stack.push("FOO", 10L + disp);
			stack.push("BAR", 20L + disp);
			stack.pop(30L + disp);
			stack.pop(40L + disp);
		}
		
		widget.setStack(stack);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		shell.dispose();
	}
	
}

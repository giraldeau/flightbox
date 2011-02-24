package org.lttng.flightbox;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.lttng.flightbox.state.VersionizedStack;
import org.lttng.flightbox.ui.StackView;

public class MainStack {

	public static void main(String[] args) {
		
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Flightbox");
		shell.setSize(800, 800);
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		
		VersionizedStack<String> versionizedStack = new VersionizedStack<String>();
		versionizedStack.push("FOO", 10L);
		versionizedStack.push("BAR", 15L);
		versionizedStack.push("BAZ", 20L);
		versionizedStack.pop(25L);
		versionizedStack.pop(30L);
		versionizedStack.pop(35L);
		
		StackView view = new StackView(shell, SWT.NONE);
		view.setStack(versionizedStack);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		shell.dispose();
		
	}
	
}

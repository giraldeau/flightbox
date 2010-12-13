package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SettingsDialog extends Dialog {

	public SettingsDialog(Shell parent) {
		super(parent);
	}

	public void open() {
		Shell parent = getParent();
		Shell dialog = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
		dialog.setSize(100,100);
		dialog.setText("Settings");
		dialog.open();
		Display display = parent.getDisplay();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	
}

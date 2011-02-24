package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.lttng.flightbox.state.VersionizedStack;

public class StackView extends Composite {

	StackWidget stackWidget;
	
	public StackView(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		stackWidget = new StackWidget(this, SWT.None);
	}

	public void setStack(VersionizedStack<String> versionizedStack) {
		stackWidget.setStack(versionizedStack);
	}

}

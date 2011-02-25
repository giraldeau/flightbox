package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.lttng.flightbox.state.VersionizedStack;

public class StackView extends Composite {

	StackWidget stackWidget;
	List list;
	
	public StackView(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		this.setLayout(gridLayout);
		
		GridData gridData = null;
		
		list = new List(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		list.add("default");
		gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		list.setLayoutData(gridData);
		
		stackWidget = new StackWidget(this, SWT.None);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		stackWidget.setLayoutData(gridData);
		stackWidget.setRowHeight(list.getItemHeight());
	}

	public void setStack(VersionizedStack<String> versionizedStack) {
		stackWidget.addStack(versionizedStack);
		stackWidget.addStack(versionizedStack);
		stackWidget.addStack(versionizedStack);
	}

}

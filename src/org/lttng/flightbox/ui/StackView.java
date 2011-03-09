package org.lttng.flightbox.ui;

import java.util.Set;
import java.util.SortedSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.lttng.flightbox.state.VersionizedStack;
import org.lttng.flightbox.state.VersionizedStack.Item;

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
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		stackWidget = new StackWidget(scrolledComposite, SWT.BORDER);
		stackWidget.setRowHeight(list.getItemHeight());
		stackWidget.setSize(10000, 100);
		scrolledComposite.setContent(stackWidget);
		
		gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		scrolledComposite.setLayoutData(gridData);
		
	}

	public void setStack(VersionizedStack<String> versionizedStack) {
		stackWidget.addStack(versionizedStack);
		SortedSet<VersionizedStack<String>.Item<String>> history = versionizedStack.getHistory();
		setTimeInterval(history.first().id, history.last().id);
	}

	public void setTimeInterval(Long startTime, Long endTime) {
		stackWidget.setRange(startTime, endTime);
	}

}

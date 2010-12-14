package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ProcessUsageView extends Composite {

	public ProcessUsageView(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		Table table = new Table(this, SWT.BORDER);
		TableColumn tc1 = new TableColumn(table, SWT.LEFT);
		TableColumn tc2 = new TableColumn(table, SWT.LEFT);
		TableColumn tc3 = new TableColumn(table, SWT.LEFT);
		tc1.setText("PID");
		tc2.setText("Command");
		tc3.setText("Running");
		tc1.setWidth(100);
		tc2.setWidth(100);
		tc3.setWidth(100);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableItem item1 = new TableItem(table, SWT.NONE);
		item1.setText(new String[] {new Integer(1234).toString(), "/usr/bin/bidon", new Double(2.34).toString()});
		TableItem item2 = new TableItem(table, SWT.NONE);
		item2.setText(new String[] {new Integer(5678).toString(), "/usr/bin/gazou", new Double(1.28).toString()});
	}
	
}

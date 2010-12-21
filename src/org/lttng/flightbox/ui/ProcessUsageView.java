package org.lttng.flightbox.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.lttng.flightbox.GlobalState.KernelMode;
import org.lttng.flightbox.TimeStats;
import org.lttng.flightbox.UsageStats;
import org.lttng.flightbox.cpu.KernelProcess;

public class ProcessUsageView extends Composite {

	private Table table;
	private TreeMap<Long, KernelProcess> procInfo;
	private UsageStats<Long> procStats;
	private ArrayList<TableData> dataSet;
	private Comparator cmp;
	private Boolean isReverse;
	private double t1;
	private double t2;
	private CpuUsageView cpuView;
	
	public ProcessUsageView(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		
		table = new Table(this, SWT.BORDER);
		TableColumn tc1 = new TableColumn(table, SWT.LEFT);
		TableColumn tc2 = new TableColumn(table, SWT.LEFT);
		TableColumn tc3 = new TableColumn(table, SWT.LEFT);
		tc1.setText("PID");
		tc2.setText("Command");
		tc3.setText("Running");
		tc1.setWidth(100);
		tc2.setWidth(300);
		tc3.setWidth(100);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		cmp = new TimeComparator();
		dataSet = new ArrayList<TableData>();
		isReverse = true;
		
		tc1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setComparator(new PidComparator());
				toggleSortOrder();
				sortTable();
			}
		});
		tc2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setComparator(new CmdComparator());
				toggleSortOrder();
				sortTable();
			}
		});
		tc3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setComparator(new TimeComparator());
				toggleSortOrder();
				sortTable();
			}
		});
	}

	class TableData {
		public String cmd;
		public Long pid;
		public Double time;
	}
	
	class CmdComparator implements Comparator {
		@Override
		public int compare(Object obj1, Object obj2) {
			TableData d1 = (TableData)obj1;
			TableData d2 = (TableData)obj2;
			return d1.cmd.compareTo(d2.cmd);
		}
	}
	
	class PidComparator implements Comparator {
		@Override
		public int compare(Object obj1, Object obj2) {
			TableData d1 = (TableData)obj1;
			TableData d2 = (TableData)obj2;
			return d1.pid.compareTo(d2.pid);
		}
	}
	
	class TimeComparator implements Comparator {
		@Override
		public int compare(Object obj1, Object obj2) {
			TableData d1 = (TableData)obj1;
			TableData d2 = (TableData)obj2;
			return d1.time.compareTo(d2.time);
		}
	}
	
	public void setStats(UsageStats<Long> procStats,
			TreeMap<Long, KernelProcess> procInfo) {
		
		this.procStats = procStats;
		this.procInfo = procInfo;
	}

	public void updateDataSet() {
		//dataSet.clear();
		dataSet = new ArrayList<TableData>();
		for(Long pid: procStats.idSet()) {
			if (pid == 0) {
				continue;
			}
			TableData elem = new TableData();
			elem.cmd = procInfo.get(pid).getCmd();
			elem.pid = pid;
			elem.time = procStats.getStats(pid).getSum(t1, t2).getTime(KernelMode.USER); 
			dataSet.add(elem);
		}
		sortTable();
	}
	
	public void sortTable() {
		Collections.sort(dataSet, cmp);
		
		table.removeAll();
		
		if (isReverse) {
			TableData elem;
			for (int i=dataSet.size()-1;i>=0;i--) {
				TableItem item = new TableItem(table, SWT.NONE);
				elem = dataSet.get(i);
				item.setText(new String[] {elem.pid.toString(), elem.cmd, new Double(elem.time/TimeStats.NANO).toString()});			
			}
		} else {
			for (TableData elem: dataSet) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(new String[] {elem.pid.toString(), elem.cmd, new Double(elem.time/TimeStats.NANO).toString()});			
			}
		}

	}
	
	public void setComparator(Comparator cmp) {
		this.cmp = cmp;
	}
	
	private void toggleSortOrder() {
		isReverse = !isReverse;
	}
	
	public void resetSumInterval() {
		if (procStats != null) {
			t1 = procStats.getStart();
			t2 = procStats.getEnd();
			updateDataSet();
		}
	}

	public void setCpuView(CpuUsageView cpuView) {
		this.cpuView = cpuView;
	}
	
	public void setSumInterval(double t1, double t2) {
		if (t2 > t1) {
			this.t1 = t1;
			this.t2 = t2;
		} else {
			this.t1 = t2;
			this.t2 = t1;
		}
		if (procStats != null) {
			updateDataSet();
		}
	}
	
}

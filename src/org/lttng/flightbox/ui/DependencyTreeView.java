package org.lttng.flightbox.ui;

import java.io.File;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.dep.BlockingTree;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.state.SyscallInfo;

public class DependencyTreeView extends Composite {

	private final TableTreeViewer ttv;
	private SystemModel model;
	private Task rootTask;
	private BlockingTaskListener blocking;

	public DependencyTreeView(Composite arg0, int arg1) {
		super(arg0, arg1);
		this.setLayout(new FillLayout());
		ttv = new TableTreeViewer(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		ttv.setContentProvider(new DepContentProvider());
		ttv.setLabelProvider(new DepLabelProvider());

		Table table = ttv.getTableTree().getTable();
	    new TableColumn(table, SWT.LEFT).setText("PID");
	    new TableColumn(table, SWT.LEFT).setText("Command");
	    new TableColumn(table, SWT.LEFT).setText("System call");
	    new TableColumn(table, SWT.LEFT).setText("Duration (ms)");
	    new TableColumn(table, SWT.LEFT).setText("Wake up");

	    table.setHeaderVisible(true);
	    table.setLinesVisible(true);
	    //ttv.reveal(ttv.getElementAt(0));
	    ttv.expandAll();
	    resizeColumns();

	    table.addListener(SWT.Expand, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				resizeColumns();
			}
	    });
	    table.addListener(SWT.Collapse, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				resizeColumns();
			}
	    });

	}

	private void resizeColumns() {
		Table table = ttv.getTableTree().getTable();
		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
	    	table.getColumn(i).pack();
	    }
	}

	public void setInput(Object node) {
		ttv.setInput(node);
		ttv.refresh();
		resizeColumns();
	}

	public void setModel(SystemModel model) {
		this.model = model;
	}

	public SystemModel getModel() {
		return model;
	}

	class DepLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener arg0) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object arg0, String arg1) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener arg0) {
		}

		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}

		@Override
		public String getColumnText(Object arg0, int arg1) {
			BlockingTree item = (BlockingTree) arg0;
			String str = "";
			switch (arg1) {
			case 0:
				int pid = item.getTask().getProcessId();
				str = String.format("%d", pid);
				break;
			case 1:
				str = item.getTask().getCmd();
				str = new File(str).getName();
				break;
			case 2:
				SyscallInfo waitingSyscall = item.getWaitingSyscall();
				int syscallId = waitingSyscall.getSyscallId();
				if (model == null) {
					str = String.format("%d", syscallId);
				} else {
					str = model.getSyscallTable().get(syscallId);
				}
				break;
			case 3:
				SyscallInfo info = item.getWaitingSyscall();
				long delay = info.getEndTime() - info.getStartTime();
				str = String.format("%d", delay / 1000000);
				break;
			case 4:
				if (item.getWakeUp() != null)
					str = item.getWakeUp().toString();
				break;
			}
			return str;
		}

	}

	class DepContentProvider implements ITreeContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}

		@Override
		public Object[] getChildren(Object arg0) {
			TreeSet<BlockingTree> children = ((BlockingTree)arg0).getChildren();
			return children == null ? new Object[0] : children.toArray();
		}

		@Override
		public Object[] getElements(Object arg0) {
			return ((TreeSet)arg0).toArray();
		}

		@Override
		public Object getParent(Object arg0) {
			return ((BlockingTree)arg0).getParent();
		}

		@Override
		public boolean hasChildren(Object arg0) {
			TreeSet<BlockingTree> children = ((BlockingTree)arg0).getChildren();
			return children == null ? false : !children.isEmpty();
		}
	}

	public void setRootTask(Task task) {
		this.rootTask = task;
		if (blocking != null) {
			setInput(blocking.getBlockingItemsForTask(task));
		}
	}

	public void setBlocking(BlockingTaskListener blocking) {
		this.blocking = blocking;
	}

	public BlockingTaskListener getBlocking() {
		return blocking;
	}

}

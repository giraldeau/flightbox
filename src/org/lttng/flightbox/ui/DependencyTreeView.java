package org.lttng.flightbox.ui;

import java.io.File;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.lttng.flightbox.dep.BlockingItem;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.state.SyscallInfo;

public class DependencyTreeView extends Composite {

	private final TreeViewer ttv;
	private SystemModel model;
	private Task rootTask;

	public DependencyTreeView(Composite arg0, int arg1) {
		super(arg0, arg1);
		this.setLayout(new FillLayout());
		ttv = new TreeViewer(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		ttv.setContentProvider(new DepContentProvider());
		ttv.setLabelProvider(new DepLabelProvider());

		Tree tree = ttv.getTree();
	    new TreeColumn(tree, SWT.LEFT).setText("PID");
	    new TreeColumn(tree, SWT.LEFT).setText("Command");
	    new TreeColumn(tree, SWT.LEFT).setText("System call");
	    new TreeColumn(tree, SWT.RIGHT).setText("Duration (ms)");
	    new TreeColumn(tree, SWT.LEFT).setText("Wake up");

	    tree.setHeaderVisible(true);
	    tree.setLinesVisible(true);
	    //ttv.reveal(ttv.getElementAt(0));
	    ttv.expandAll();
	    resizeColumns();

	    tree.addListener(SWT.Expand, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				resizeColumns();
			}
	    });
	    tree.addListener(SWT.Collapse, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				resizeColumns();
			}
	    });

	}

	private void resizeColumns() {
		Tree tree = ttv.getTree();
		for (int i = 0, n = tree.getColumnCount(); i < n; i++) {
	    	tree.getColumn(i).pack();
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
			BlockingItem item = (BlockingItem) arg0;
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
				str = String.format("%.3f", delay/ 1000000.0);
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
			TreeSet<BlockingItem> children = ((BlockingItem)arg0).getChildren(model);
			return children == null ? new Object[0] : children.toArray();
		}

		@Override
		public Object[] getElements(Object arg0) {
			return ((TreeSet<?>)arg0).toArray();
		}

		@Override
		public Object getParent(Object arg0) {
			return null;
		}

		@Override
		public boolean hasChildren(Object arg0) {
			TreeSet<BlockingItem> children = ((BlockingItem)arg0).getChildren(model);
			return children == null ? false : !children.isEmpty();
		}
	}

	public void setRootTask(Task task) {
		this.rootTask = task;
		if (model != null) {
			setInput(model.getBlockingModel().getBlockingItemsForTask(task));
		}
	}

}

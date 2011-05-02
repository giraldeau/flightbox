package org.lttng.flightbox.ui;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.statistics.ResourceUsage;
import org.lttng.flightbox.ui.ProcessUsageView.TableData;

public class DependencyView extends Composite {

    private ProcessUsageView depProcessView;
    private DependencyTreeView depTreeView;
    private CTabItem depSummaryItem;
    private DependencySummaryView dependencySummaryView;

    public DependencyView(Composite parent, int flags) {
        super(parent, flags);

        SashForm depSashForm = new SashForm(this, SWT.VERTICAL);
        depProcessView = new ProcessUsageView(depSashForm, SWT.BORDER);
        
        CTabFolder folder = new CTabFolder(depSashForm, SWT.NONE);
        depSummaryItem = new CTabItem(folder, SWT.NONE);
        depSummaryItem.setText("Summary");
        dependencySummaryView = new DependencySummaryView(folder, SWT.NONE);
        dependencySummaryView.setLayout(new FillLayout());
        depSummaryItem.setControl(dependencySummaryView);
        
        CTabItem processTabItem = new CTabItem(folder, SWT.NONE);
        processTabItem.setText("Detail");
        depTreeView = new DependencyTreeView(folder, SWT.BORDER);
        depTreeView.setLayout(new FillLayout());
        processTabItem.setControl(depTreeView);

        // default tab at start
        folder.setSelection(depSummaryItem);
        
        depProcessView.getTable().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                TableData data = (TableData) arg0.item.getData();
                HashMap<Integer, TreeSet<Task>> tasks = depTreeView.getModel().getTasks();
                Integer pid = new Long(data.pid).intValue();
                if (tasks.containsKey(pid)) {
                    TreeSet<Task> taskSet = tasks.get(pid);
                    if (taskSet != null && !taskSet.isEmpty()) {
                        Task latest = taskSet.last();
                        depTreeView.setRootTask(latest);
                        dependencySummaryView.setTask(latest);
                    }
                }
            }
        });
    }

    public void setStats(ResourceUsage<Long> procStats, TreeMap<Long, Task> procInfo) {
        depProcessView.setStats(procStats, procInfo);
    }

    public void setModel(SystemModel model) {
        depTreeView.setModel(model);
        dependencySummaryView.setModel(model);
    }

}

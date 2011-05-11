package org.lttng.flightbox.ui;

import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.statistics.BucketSeries;
import org.lttng.flightbox.statistics.ResourceUsage;
import org.lttng.flightbox.ui.ProcessUsageView.TableData;

public class CpuUsageView extends Composite {

    private CpuUsageChart cpuView;
    private ProcessUsageView processView;

    public CpuUsageView(Composite parent, int flags) {
        super(parent, flags);
        SashForm sashForm = new SashForm(this, SWT.VERTICAL);
        cpuView = new CpuUsageChart(sashForm, SWT.BORDER);
        processView = new ProcessUsageView(sashForm, SWT.BORDER);
        cpuView.setProcessView(processView);
        
        processView.getTable().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                TableData data = (TableData) arg0.item.getData();
                ResourceUsage<Long> procStats = processView.getStats();
                ResourceUsage<Long> cpuStats = cpuView.getStats();
                BucketSeries series = procStats.getStats(data.pid);
                
                if (cpuStats != null && procStats != null && cpuStats.getNumEntry() > 0 && series != null) {
                	double[] xSeries = series.getXSeries();
                	double[] ySeries = series.getYSeries(TaskState.USER);
                	double[] yScaled = new double[ySeries.length];
                	double factor = 1.0 / cpuStats.getNumEntry();
                	for (int i = 0; i<ySeries.length; i++) {
                		yScaled[i] = factor * ySeries[i];
                	}
                	cpuView.setProcessSeries(xSeries, yScaled);
                }
            }
        });

    }

    public void setCpuStats(ResourceUsage<Long> cpuStats) {
        cpuView.setCpuStats(cpuStats);
    }

    public void setStats(ResourceUsage<Long> procStats, TreeMap<Long, Task> procInfo) {
        processView.setStats(procStats, procInfo);
    }

}

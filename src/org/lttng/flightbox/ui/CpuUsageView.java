package org.lttng.flightbox.ui;

import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.statistics.ResourceUsage;

public class CpuUsageView extends Composite {

    private CpuUsageChart cpuView;
    private ProcessUsageView processView;

    public CpuUsageView(Composite parent, int flags) {
        super(parent, flags);
        SashForm sashForm = new SashForm(this, SWT.VERTICAL);
        cpuView = new CpuUsageChart(sashForm, SWT.BORDER);
        processView = new ProcessUsageView(sashForm, SWT.BORDER);
        cpuView.setProcessView(processView);

    }

    public void setCpuStats(ResourceUsage<Long> cpuStats) {
        cpuView.setCpuStats(cpuStats);
    }

    public void setStats(ResourceUsage<Long> procStats, TreeMap<Long, Task> procInfo) {
        processView.setStats(procStats, procInfo);
    }

}

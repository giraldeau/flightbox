package org.lttng.flightbox.ui;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.lttng.flightbox.dep.BlockingStats;
import org.lttng.flightbox.dep.BlockingSummaryStatistics;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

/*
 * based on http://www.vogella.de/articles/EclipseJFaceTable/article.html
 */

public class DependencySummaryView  extends Composite {

    TableViewer tableViewer;
    private SystemModel model;

    private static final String fmtMs = "%.3f";
    private static final String fmtInt = "%d";
    
    DependencySummaryView(Composite parent, int flags) {
        super(parent, flags);
        this.setLayout(new FillLayout());
        tableViewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        
        createColumns(this, tableViewer);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        tableViewer.setContentProvider(new ArrayContentProvider());
        
        BlockingStats blockingStats = new BlockingStats();
        
        tableViewer.setInput(blockingStats.getBlockingStats());
        
        // Layout the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        tableViewer.getControl().setLayoutData(gridData);
    }

    private void createColumns(Composite parent, TableViewer viewer) {
        String[] titles = { "System call", "N", "Sum (ms)", "Min (ms)", "Max (ms)", "Mean (ms)", "Stddev (ms)" };
        int bounds[] = { 100, 100, 100, 100, 100, 100, 100 };
        
        TableViewerColumn col = null;

        // System call
        col = createTableViewerColumn(titles[0], bounds[0], 0);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object obj) {
                BlockingSummaryStatistics stat = (BlockingSummaryStatistics) obj;
                String s = "";
                if (model != null) {
                    s = model.getSyscallTable().get(stat.getSyscallId());
                } else {
                    s = String.format("%d", stat.getSyscallId());
                }
                return s;
            }
        });
        
        // N
        col = createTableViewerColumn(titles[1], bounds[1], 1);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object obj) {
                BlockingSummaryStatistics stat = (BlockingSummaryStatistics) obj;
                return String.format(fmtInt, stat.getSummary().getN());
            }
        });
        
        // Sum
        col = createTableViewerColumn(titles[2], bounds[2], 2);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object obj) {
                BlockingSummaryStatistics stat = (BlockingSummaryStatistics) obj;
                return String.format(fmtMs, stat.getSummary().getSum() / 1000000);
            }
        });
        
        // Min
        col = createTableViewerColumn(titles[3], bounds[3], 3);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object obj) {
                BlockingSummaryStatistics stat = (BlockingSummaryStatistics) obj;
                return String.format(fmtMs, stat.getSummary().getMin() / 1000000);
            }
        });
        
        // Max
        col = createTableViewerColumn(titles[4], bounds[4], 4);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object obj) {
                BlockingSummaryStatistics stat = (BlockingSummaryStatistics) obj;
                return String.format(fmtMs, stat.getSummary().getMax() / 1000000);
            }
        });
        
        // Mean
        col = createTableViewerColumn(titles[5], bounds[5], 5);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object obj) {
                BlockingSummaryStatistics stat = (BlockingSummaryStatistics) obj;
                return String.format(fmtMs, stat.getSummary().getMean() / 1000000);
            }
        });
        
        // Stddev
        col = createTableViewerColumn(titles[5], bounds[5], 5);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object obj) {
                BlockingSummaryStatistics stat = (BlockingSummaryStatistics) obj;
                return String.format(fmtMs, stat.getSummary().getStandardDeviation() / 1000000);
            }
        });
    }

    private TableViewerColumn createTableViewerColumn(String title, int bound, int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        return viewerColumn;
    }

    public void setModel(SystemModel model) {
        this.model = model;
    }

    public void setTask(Task task) {
        BlockingStats items = model.getBlockingModel().getBlockingStatsForTask(task);
        tableViewer.setInput(items.getBlockingStats());
    }

}

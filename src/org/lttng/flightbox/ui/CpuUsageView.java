package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.lttng.flightbox.UsageStats;
import org.lttng.flightbox.model.KernelTask.TaskState;
import org.lttng.flightbox.ui.ChartHighlighter.TimeInterval;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

public class CpuUsageView extends Composite {

	UsageStats<Long> stats;
	Chart chart;
	ChartHighlighter highlighter;
	double t1;
	double t2;
	private ProcessUsageView processView;
	
	public CpuUsageView(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		
		chart = new Chart(this, SWT.NONE);
		highlighter = new ChartHighlighter(chart, SWT.NONE);
		
		// set titles
		chart.getTitle().setText("CPU Usage according to time");
		chart.getAxisSet().getXAxis(0).getTitle().setText("Time");
		chart.getAxisSet().getYAxis(0).getTitle().setText("CPU Usage");
		
		Listener listener = new Listener() {

			int down;
			boolean track = false;
			
			@Override
			public void handleEvent(Event e) {
				switch (e.type){
				case SWT.MouseDown:
					down = e.x;
					track = true;
					break;
				case SWT.MouseMove:
					if (track) { 
						//updateAll(e);
						highlighter.setPixelInterval(down, e.x);
						chart.redraw();		
					}
					break;
				case SWT.MouseUp:
					updateAll(e);
					track = false;
					break;
				}
			}
			
			public void updateAll(Event e) {
				highlighter.setPixelInterval(down, e.x);
				if (processView != null) {
					TimeInterval interval = highlighter.getInterval();
					if (Math.abs(down - e.x) == 0){
						processView.resetSumInterval();
					} else {
						processView.setSumInterval(interval.t1, interval.t2);
					}
					processView.sortTable();
				}
				chart.redraw();
			}
		};
		
		chart.getPlotArea().addListener(SWT.MouseDown, listener);
		chart.getPlotArea().addListener(SWT.MouseUp, listener);
		chart.getPlotArea().addListener(SWT.MouseMove, listener);
	}
	

	public void setCpuStats(UsageStats<Long> stats) {
		this.stats = stats;
	}
	
	public void updateData() {
		// clear the chart
		ISeriesSet set = chart.getSeriesSet();
		for (ISeries s: set.getSeries()) {
			set.deleteSeries(s.getId());
		}
		if (stats != null) {
			// recompute the chart
			for(int i=0; i<stats.getNumEntry(); i++) {
				double[] dataX = stats.getXSeries(new Long(i));
				double[] dataY = stats.getYSeries(new Long(i), TaskState.USER);
				ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet()
				.createSeries(SeriesType.LINE, "CPU " + i);
				lineSeries.setSymbolType(PlotSymbolType.NONE);
				lineSeries.setXSeries(dataX);
				lineSeries.setYSeries(dataY);
				lineSeries.setAntialias(SWT.ON);
				//lineSeries.setLineColor(color);
			}
		}
		// adjust the axis range
		chart.getAxisSet().adjustRange();
		chart.getAxisSet().getYAxis(0).setRange(new Range(0, 1.1));
		chart.redraw();
	}


	public void setProcessView(ProcessUsageView processView) {
		this.processView = processView;
	}


	public void resetHighlight() {
		highlighter.setPixelInterval(0, 0);
	}

}

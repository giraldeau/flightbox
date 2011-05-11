package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.statistics.ResourceUsage;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.IPlotArea;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

public class CpuUsageChart extends Composite {

	ResourceUsage<Long> stats;
	Chart chart;
	ChartHighlighter highlighter;
	double t1;
	double t2;
	private ProcessUsageView processView;
	
	public CpuUsageChart(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		
		chart = new Chart(this, SWT.NONE);
		IPlotArea plotArea = (IPlotArea) chart.getPlotArea();
		highlighter = new ChartHighlighter();
		plotArea.addCustomPaintListener(highlighter);
		chart.addControlListener(new ControlListener() {

			@Override
			public void controlResized(ControlEvent arg0) {
				Composite area = chart.getPlotArea();
				Rectangle bounds = area.getBounds();
				highlighter.setHeight(bounds.height);
				highlighter.setWidth(bounds.width);
			}

			@Override
			public void controlMoved(ControlEvent arg0) {
			}
			
		});
		
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
					IAxis xAxis = chart.getAxisSet().getXAxis(0);
					double x1 = xAxis.getDataCoordinate(down);
					double x2 = xAxis.getDataCoordinate(e.x);
					if (Math.abs(down - e.x) == 0){
						processView.resetSumInterval();
					} else {
						processView.setSumInterval(x1, x2);
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
	

	public void setCpuStats(ResourceUsage<Long> stats) {
		this.stats = stats;
		updateData();
		resetHighlight();
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

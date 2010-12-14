package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.lttng.flightbox.GlobalState.KernelMode;
import org.lttng.flightbox.UsageStats;
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
	public CpuUsageView(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		
		chart = new Chart(this, SWT.NONE);
		
		// set titles
		chart.getTitle().setText("CPU Usage according to time");
		chart.getAxisSet().getXAxis(0).getTitle().setText("Time");
		chart.getAxisSet().getYAxis(0).getTitle().setText("CPU Usage");
		chart.getPlotArea().addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				//System.out.println(e);
				int w = chart.getPlotArea().getSize().x;
				double rel = (double) e.x / (double) w;
				Range range = chart.getAxisSet().getXAxis(0).getRange();
				double span = range.upper - range.lower;
				double val = span * rel;
				double pos = val + range.lower; 
				System.out.println("pos=" + pos);
			}
		});
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
				double[] dataY = stats.getYSeries(new Long(i), KernelMode.USER);
				ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet()
				.createSeries(SeriesType.LINE, "CPU ");
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
	
}

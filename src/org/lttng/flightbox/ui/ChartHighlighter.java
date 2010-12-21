package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ICustomPaintListener;

public class ChartHighlighter extends Composite implements ICustomPaintListener {
	
	private Image imageCache;
	private ImageData sourceData;
	private double d1;
	private double d2;
	private Chart chart;
	private boolean updateImageCache;
	private Color bg;
	private Color fg;
	private PaletteData palette;

	public class TimeInterval {
		public double t1; 
		public double t2;
		public TimeInterval(double t1, double t2) {
			this.t1 = t1; 
			this.t2 = t2;
		}
	}
	
	public ChartHighlighter(Chart parent, int style) {
		super(parent, style);
		chart = parent;
		chart.addCustomPaintListener(this);
		Display display = Display.getCurrent();
		bg = display.getSystemColor(SWT.COLOR_WHITE);
	    fg = display.getSystemColor(SWT.COLOR_BLACK);
	    palette = new PaletteData(new RGB[] { bg.getRGB(), fg.getRGB() });
		updateImageCache = true;
	}
	
	public void setInterval(double d1, double d2) {
		this.d1 = d1;
		this.d2 = d2;
		updateImageCache = true;
	}
	
	public TimeInterval getInterval() {
		return new TimeInterval(this.d1, this.d2);
	}
	
	public void setPixelInterval(int x1, int x2) {
		IAxis xAxis = chart.getAxisSet().getXAxis(0);
		d1 = xAxis.getDataCoordinate(x1);
		d2 = xAxis.getDataCoordinate(x2);
		updateImageCache = true;
	}
		
	@Override
	public void paintControl(PaintEvent e) {
		Display display = Display.getCurrent();
		if (updateImageCache) {
			if (imageCache != null && !imageCache.isDisposed()) {
                imageCache.dispose();
            }
			IAxis xAxis = chart.getAxisSet().getXAxis(0);
			int x1 = xAxis.getPixelCoordinate(d1);
			int x2 = xAxis.getPixelCoordinate(d2);
			
			Composite area = chart.getPlotArea();
			Point size = area.getSize();
		    sourceData = new ImageData(size.x, size.y, 1, palette);
		    sourceData.transparentPixel = 0;
			imageCache = new Image(display, sourceData);

			GC gc = new GC(imageCache);
			gc.setAntialias(1);
			gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
			
			gc.fillRectangle(x1, 0, x2 - x1, size.y);
			gc.dispose();
			updateImageCache = false;
		}
		
		/* FIXME: why there is always a white opaque transparent background
		 * to the image without using a palette?
		 */

		int oldAlpha = e.gc.getAlpha();
		e.gc.setAlpha(128);
		e.gc.drawImage(imageCache, 0, 0);
		e.gc.setAlpha(oldAlpha);
	}

	public void save(String path) {
		if (imageCache == null) {
			return;
		}
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] {imageCache.getImageData()};
		imageLoader.save(path, SWT.IMAGE_BMP);
	}

	@Override
	public boolean drawBehindSeries() {
		return false;
	}
	
    /*
     * @see Control#update()
     */
    @Override
    public void update() {
        super.update();
        updateImageCache = true;
    }

    /*
     * @see Control#redraw()
     */
    @Override
    public void redraw() {
        super.redraw();
        updateImageCache = true;
    }

    /*
     * @see Widget#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (imageCache != null && !imageCache.isDisposed()) {
            imageCache.dispose();
        }
    }
}

package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;

public class PlotAreaHighlighter extends Composite implements PaintListener {

	Color highlightColor;
	private int x1;
	private int x2;
	private boolean updateImageCache;
	private Image imageCache;

	public PlotAreaHighlighter(Chart chart, int style) {
		super(chart, style);
		highlightColor = new Color(Display.getDefault(), 0, 0, 255);
		updateImageCache = true;
		addPaintListener(this);
		this.x1 = 0;
		this.x2 = 0;

	}

	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
	}

	public void setInterval(int x1, int x2) {
		this.x1 = x1;
		this.x2 = x2;
		updateImageCache = true;
	}

	public void paintControl(PaintEvent e) {
		System.out.println("test");
		if (updateImageCache) {
			Point p = getSize();
			if (imageCache != null && !imageCache.isDisposed()) {
				imageCache.dispose();
			}
			if(p.x == 0)
			{
				throw new RuntimeException();
			}
			Display d1 = Display.getCurrent();
			Display d2 = Display.getDefault();
			imageCache = new Image(Display.getCurrent(), p.x, p.y);
			GC gc = new GC(imageCache);
			gc.setAlpha(128);

			gc.setBackground(highlightColor);
			//gc.setForeground(highlightColor);
			gc.fillRectangle(x1, 0, x2, p.y);
			gc.dispose();
			updateImageCache = false;

			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { imageCache.getImageData() };
			imageLoader.save("test.bmp", SWT.IMAGE_BMP);
		}
		e.gc.drawImage(imageCache, 0, 0);
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

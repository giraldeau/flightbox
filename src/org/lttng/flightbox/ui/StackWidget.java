package org.lttng.flightbox.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.lttng.flightbox.state.Interval;
import org.lttng.flightbox.state.VersionizedStack;
import org.lttng.flightbox.state.VersionizedStack.Item;

public class StackWidget extends Canvas {

	Image imageCache;
	boolean updateImageCache;
	Color bgColor;
	Map<String, Color> legend;
	List<VersionizedStack<String>> stacks;
	Colorizer colorizer;
	Long t1;
	Long t2;
	int rowHeight;
	int padding;
	
	public int getRowHeight() {
		return rowHeight;
	}

	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}

	public StackWidget(Composite parent, int style) {
		super(parent, style);
		updateImageCache = true;
		Display display = getDisplay();
		bgColor = display.getSystemColor(SWT.COLOR_BLACK);
		colorizer = new Colorizer();
		legend = new HashMap<String, Color>();
		stacks = new ArrayList<VersionizedStack<String>>();
		
		t1 = 0L;
		t2 = 100L;
		rowHeight = 23;
		padding = 6;
		
	    addDisposeListener(new DisposeListener() {
	    	public void widgetDisposed(DisposeEvent e) {
	    		StackWidget.this.widgetDisposed(e);
	    	}
	    });

	    addPaintListener(new PaintListener() {
	    	public void paintControl(PaintEvent e) {
	    		StackWidget.this.paintControl(e);
	    	}
	    });
	}

	protected void paintControl(PaintEvent e) {
		Display display = Display.getCurrent();
		if (updateImageCache) {
			if (imageCache != null && !imageCache.isDisposed()) {
                imageCache.dispose();
            }
			Rectangle clientArea = getClientArea();
			imageCache = new Image(display, clientArea.width, clientArea.height);

			GC gc = new GC(imageCache);

			setForeground(bgColor);
			gc.fillRectangle(clientArea);
			
			drawStacks(gc);
			
			gc.dispose();
			updateImageCache = false;
		}
		e.gc.drawImage(imageCache, 0, 0);
	}

	private void drawStacks(GC gc) {
		if (legend.size() == 0)
			return;
		int y1 = 0 + padding;
		int y2 = rowHeight - padding;
		for (VersionizedStack<String> stack: stacks) {
			drawOneStack(gc, stack, y1, y2);
			y1 += rowHeight;
			y2 += rowHeight;	
		}
		
	}

	private void drawOneStack(GC gc, VersionizedStack<String> stack, int y1, int y2) {
		Color color;
		for (Iterator<Interval> iter = stack.iterator(); iter.hasNext();) {
			Interval interval = iter.next();
			color = legend.get(interval.content);
			gc.setBackground(color);
			int x1 = getPixedCoordinate(interval.t1);
			int x2 = getPixedCoordinate(interval.t2);
			int width = x2 - x1;
			gc.fillRectangle(x1, y1, width, y2 - y1);
		}
	}
	
	public int getPixedCoordinate(Long t) {
		Rectangle clientArea = getClientArea();
		Long ts = t2 - t1;
		int res = (int) (((double)t / (double)ts) * clientArea.width);
		return res;
	}
	
	protected void widgetDisposed(DisposeEvent e) {
		bgColor.dispose();
		for (Color c: legend.values()) {
			c.dispose();
		}
		if (imageCache != null && !imageCache.isDisposed()) {
            imageCache.dispose();
        }		
	}

	public Collection<VersionizedStack<String>> getStacks() {
		return stacks;
	}
	
	public void addAllStacks(Collection<VersionizedStack<String>> stacks) {
		for (VersionizedStack<String> stack: stacks) {
			addStack(stack);
		}
	}

	public void addStack(VersionizedStack<String> stack) {
		this.stacks.add(stack);
		Display display = getDisplay();
		for (String sym: stack.getSymbols()) {
			RGB rgb = colorizer.getColor(sym);
			Color c = new Color(display, rgb);
			legend.put(sym, c);
		}
		redraw();
	}
	
	public void setRange(Long t1, Long t2) {
		this.t1 = t1;
		this.t2 = t2;
		redraw();
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int width = 0, height = 0;
		
		Rectangle clientArea = getClientArea();
		width = clientArea.width;
		height = clientArea.height;
		if (wHint != SWT.DEFAULT)
			width = wHint;
		if (hHint != SWT.DEFAULT)
			height = hHint;
		return new Point(width, height);
	}
	
	public void save(String path) {
		if (imageCache == null) {
			return;
		}
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] {imageCache.getImageData()};
		imageLoader.save(path, SWT.IMAGE_BMP);
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
    }
	
}

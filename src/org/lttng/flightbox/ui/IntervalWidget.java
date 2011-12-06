package org.lttng.flightbox.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

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

public class IntervalWidget extends Canvas {

	VersionizedStack<String> stack;
	Image imageCache;
	Long t1;
	Long t2;
	Color bgColor;
	Colorizer colorizer;
	int padding;
	boolean updateImageCache;
	int rowHeight;
	HashMap<String, Color> legend;
	
	ImageProxy imageProxy;
	
	public IntervalWidget(Composite parent, int opts) {
		super(parent, opts);
		updateImageCache = true;
		t1 = 0L;
		t2 = 0L;
		Display display = getDisplay();
		bgColor = display.getSystemColor(SWT.COLOR_BLACK);
		colorizer = Colorizer.getInstance();
		rowHeight = 23;
		padding = 6;
		legend = new HashMap<String, Color>();
		imageProxy = new ImageProxy();
		imageProxy.setRender(new IntervalRender());
		
	    addDisposeListener(new DisposeListener() {
	    	public void widgetDisposed(DisposeEvent e) {
	    		IntervalWidget.this.widgetDisposed(e);
	    	}
	    });

	    addPaintListener(new PaintListener() {
	    	public void paintControl(PaintEvent e) {
	    		IntervalWidget.this.paintControl(e);
	    	}
	    });
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

	public void setRange(Long t1, Long t2) {
		this.t1 = t1;
		this.t2 = t2;
		redraw();
	} 
	
	public void save(String path) {
		if (imageCache == null) {
			return;
		}
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] {imageCache.getImageData()};
		imageLoader.save(path, SWT.IMAGE_BMP);
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
	
	protected void paintControl(PaintEvent e) {
		Display display = Display.getCurrent();
		if (updateImageCache) {
			if (imageCache != null && !imageCache.isDisposed()) {
                imageCache.dispose();
            }
			Rectangle clientArea = getClientArea();
			imageCache = new Image(display, clientArea.width, this.rowHeight);

			GC gc = new GC(imageCache);

			setForeground(bgColor);
			gc.fillRectangle(clientArea);
			
			drawIntervals(gc);
			
			gc.dispose();
			updateImageCache = false;
		}
		e.gc.drawImage(imageCache, 0, 0);
	}

	private void drawIntervals(GC gc) {
		
		//lol never used =D
		int y1 = padding;
		int y2 = rowHeight - padding;
		Color color;
		SortedSet<VersionizedStack<String>.Item<String>> range = stack.getRange(t1, t2);
		String start = stack.peek(t1);
		String end = stack.peekInclusive(t2);

		// if range is empty, draw only one interval
		if (range.isEmpty()) {
			if (start == null) {
				return;
			}
			if (!start.equals(end)) {
				throw new RuntimeException("state doesn't match: " + start + " " + end);
			}
			drawInterval(gc, t1, t2, start);
		}
		
		// draw first interval
		drawInterval(gc, t1, range.first().id, start);
		
		// draw middle intervals
		VersionizedStack<String>.Item<String> prev, curr;
		Iterator<VersionizedStack<String>.Item<String>> iter = range.iterator();
		prev = iter.next();
		
		while(iter.hasNext()) {
			curr = iter.next();
			drawInterval(gc, prev.id, curr.id, prev.content);
			prev = curr;
		}
		
		// draw last interval
		drawInterval(gc, range.last().id, t2, end);

	}
	
	public void drawInterval(GC gc, Long t1, Long t2, String s) {
		Color color = legend.get(s);
		gc.setBackground(color);
		int x1 = getPixedCoordinate(t1);
		int x2 = getPixedCoordinate(t2);
		int width = x2 - x1;
		gc.fillRectangle(x1, 0, width, rowHeight);
	}

	public int getPixedCoordinate(Long t) {
		Rectangle clientArea = getClientArea();
		Long ts = t2 - t1;
		int res = (int) (((double)(t - t1) * clientArea.width) / (double)ts);
		return res;
	}
	
	public VersionizedStack<String> getStack() {
		return stack;
	}

	public void setStack(VersionizedStack<String> stack) {
		imageProxy.setData(stack);
		this.stack = stack;
		Display display = getDisplay();
		for (String sym: stack.getSymbols()) {
			RGB rgb = colorizer.getColor(sym);
			Color c = new Color(display, rgb);
			legend.put(sym, c);
		}
		setRange(stack.begin(), stack.end());
		redraw();
	}

	public int getRowHeight() {
		return rowHeight;
	}

	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
		redraw();
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

	public Image getImage() {
		return imageCache;
	}
	
}

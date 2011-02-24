package org.lttng.flightbox.ui;

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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.lttng.flightbox.state.VersionizedStack;

public class StackWidget extends Canvas {

	Image imageCache;
	boolean updateImageCache;
	Color bgColor;
	VersionizedStack<String> stack;
	Long t1;
	Long t2;
	
	public StackWidget(Composite parent, int style) {
		super(parent, style);
		updateImageCache = true;
		bgColor = new Color(null, 255, 255, 255);
		setBackground(bgColor);
		
		t1 = 0L;
		t2 = 0L;
		
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				bgColor.dispose();
			}
		});
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
			gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
			
			gc.fillRectangle(10, 0, 100, 100);
			gc.dispose();
			updateImageCache = false;
		}
		e.gc.drawImage(imageCache, 0, 0);
	}

	public int getPixedCoordinate(Long t) {
		Rectangle clientArea = getClientArea();
		Long ts = t2 - t1;
		/* is it valid to cast from long to int? what happen in case of overflow? */
		return (int) ((t / ts) * clientArea.width);
	}
	
	protected void widgetDisposed(DisposeEvent e) {
		
	}

	public VersionizedStack<String> getStack() {
		return stack;
	}
	
	public void setStack(VersionizedStack<String> stack) {
		this.stack = stack;
		for(String sym: stack.getSymbols()) {
			
		}
		redraw();
	}
	
	public void setInterval(Long t1, Long t2) {
		this.t1 = t1;
		this.t2 = t2;
		redraw();
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(100, 100);
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
        if (imageCache != null && !imageCache.isDisposed()) {
            imageCache.dispose();
        }
    }
	
}

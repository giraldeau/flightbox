package org.lttng.flightbox.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.lttng.flightbox.state.VersionizedStack;

public class IntervalRender implements ImageRender {

	private VersionizedStack<String> data;
	Colorizer colorizer;
	HashMap<String, Color> legend;

	int width;
	int height;
	int padding;
	int barHeight;
	long t1;
	long t2;

	public IntervalRender() {
		t1 = 0;
		t2 = 0;
		setWidth(0);
		setHeight(23);
		setPadding(6);
		colorizer = new Colorizer();
		legend = new HashMap<String, Color>();
	}

	/* caller must dispose the image */
	@Override
	public Image render(long ts1, long ts2, int width, int height) {
		setWidth(width);
		setHeight(height);
		return render(ts1, ts2);
	}

	public Image render(long ts1, long ts2) {
		setRange(ts1, ts2);
		Color color;

		Display display = Display.getCurrent();
		Image image = new Image(display, width, height);
		GC gc = new GC(image);

		// no data or interval has no duration
		if (data == null || ts1 >= ts2) {
			gc.dispose();
			return image;
		}

		SortedSet<VersionizedStack<String>.Item<String>> range = data.getRange(
				ts1, ts2);
		String start = data.peek(ts1);
		String end = data.peekInclusive(ts2);

		// if range is empty, draw only one interval
		if (range.isEmpty()) {
			if (start == null) {
				gc.dispose();
				return image;
			}
			if (!start.equals(end)) {
				gc.dispose();
				image.dispose();
				throw new RuntimeException("state doesn't match: " + start
						+ " " + end);
			}
			drawInterval(gc, ts1, ts2, start);
		}

		// draw first interval
		drawInterval(gc, ts1, range.first().id, start);

		// draw middle intervals
		VersionizedStack<String>.Item<String> prev, curr;
		Iterator<VersionizedStack<String>.Item<String>> iter = range.iterator();
		prev = iter.next();

		while (iter.hasNext()) {
			curr = iter.next();
			drawInterval(gc, prev.id, curr.id, prev.content);
			prev = curr;
		}

		// draw last interval
		drawInterval(gc, range.last().id, ts2, end);

		gc.dispose();
		return image;

	}

	public void drawInterval(GC gc, long ts1, long ts2, String s) {
		// don't draw interval without a state
		if (s == null)
			return;
		Color color = legend.get(s);
		gc.setBackground(color);
		int x1 = getPixedCoordinate(ts1);
		int x2 = getPixedCoordinate(ts2);
		int width = x2 - x1;
		gc.fillRectangle(x1, padding, width, barHeight);
	}

	public int getPixedCoordinate(long t) {
		Long ts = t2 - t1;
		int res = (int) (((double) (t - t1) * width) / (double) ts);
		return res;
	}

	public void updateLegend() {
		Display display = Display.getCurrent();
		for (String sym : data.getSymbols()) {
			RGB rgb = colorizer.getColor(sym);
			Color c = new Color(display, rgb);
			legend.put(sym, c);
		}
	}

	@Override
	public void setDataObject(Object obj) {
		data = (VersionizedStack<String>) obj;
		t1 = data.begin();
		t2 = data.end();
		updateLegend();
	}

	public void dispose() {
		for (Color c : legend.values()) {
			c.dispose();
		}
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
		this.barHeight = height - padding * 2;
	}

	public int getPadding() {
		return padding;
	}

	public void setPadding(int padding) {
		this.padding = padding;
		this.barHeight = height - padding * 2;
	}

	@Override
	public void setRange(long t1, long t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

}

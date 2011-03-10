package org.lttng.flightbox.ui;

import org.eclipse.swt.graphics.Image;

public interface ImageRender {

	Image render(long t1, long t2, int w, int h);
	Image render(long t1, long t2);
	void setDataObject(Object obj);
	void setWidth(int w);
	void setHeight(int h);
	int getWidth();
	int getHeight();
	void setRange(long t1, long t2);
}

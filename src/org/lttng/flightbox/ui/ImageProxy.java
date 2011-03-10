package org.lttng.flightbox.ui;

public class ImageProxy {

	ImageRender render;
	Object obj;

	public void setRender(ImageRender render) {
		this.render = render;
	}

	public void setData(Object obj) {
		this.obj = obj;
	}

	public void getImage(Long t1, Long t2, int width, int height) {
		System.out.println("nsec per pix =" + (t2 - t1 / width));
	}
}

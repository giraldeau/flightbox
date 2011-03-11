package org.lttng.flightbox.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class Colorizer {

	Map<Object, RGB> colorMap;
	ArrayList<RGB> palette;
	
	static int next = 0;
	
	private static class InstanceHolder {
		public static final Colorizer instance = new Colorizer();
	}
	
	private Colorizer() {
		colorMap = new HashMap<Object, RGB>();
		palette = new ArrayList<RGB>();
		loadPalette();
	}

	public static Colorizer getInstance() {
		return InstanceHolder.instance;
	}
	
	private void loadPalette() {
		palette.add(new RGB(255,0,0));
		palette.add(new RGB(0,255,0));
		palette.add(new RGB(0,0,255));
	}

	public RGB getColor(Object obj) {
		if (!colorMap.containsKey(obj)) {
			int id = next++ % palette.size();
			RGB color = palette.get(id);
			colorMap.put(obj, color);
		}
		return colorMap.get(obj);
	}

	public ArrayList<RGB> getPalette() {
		return palette;
	}
	
}

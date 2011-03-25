package org.lttng.flightbox.histogram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class HistogramPainter {

	private int height = 100;
	BufferedImage img; 
	
	public HistogramPainter() {
		
	}
	
	public void paint(int[] samples) {
		int max = getMax(samples);
		img = new BufferedImage(samples.length, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = img.createGraphics();
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, samples.length, height);
		graphics.setColor(Color.blue);
		int sample;
		for(int i = 0; i< samples.length; i++) {
			sample = (samples[i] * height) / max;
			graphics.drawLine(i, height, i, height - sample);
		}
	}
	
	public void save(String imagePath) throws IOException {
		if (img == null)
			return;
		ImageIO.write(img, "png", new File(imagePath));
	}
	
	public int getMax(int[] samples) {
		int max = 0;
		for(int x: samples) {
			if (x > max)
				max = x;
		}
		return max;
	}
	
}

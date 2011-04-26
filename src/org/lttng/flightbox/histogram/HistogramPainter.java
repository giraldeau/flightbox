package org.lttng.flightbox.histogram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class HistogramPainter {

	private int height = 100;
	private int width = 100; 
	BufferedImage img; 
	
	public HistogramPainter() {
		
	}
	
	public void paint(int[] samples) {
		img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = img.createGraphics();
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, samples.length, getHeight());
		graphics.setColor(Color.blue);
		
		int max = getMax(samples);
        if (max == 0)
            return;
        
        int sample;
		for(int i = 0; i< samples.length; i++) {
			sample = (samples[i] * getHeight()) / max;
			graphics.drawLine(i, getHeight(), i, getHeight() - sample);
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

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int weight) {
        this.width = weight;
    }

    public int getWidth() {
        return width;
    }
	
}

package org.lttng.flightbox.xml;

import java.io.FileInputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class ManifestReader {

	public void read(String file) throws JDOMException, IOException {
		
		SAXBuilder builder = new SAXBuilder(true);
		FileInputStream inputStream = new FileInputStream(file);
		Document doc = builder.build(inputStream, "/home/francis/workspace2/flightbox/manifest/");
		System.out.println(doc);
	}
}

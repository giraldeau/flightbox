package org.lttng.flightbox.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class ManifestReader {
	
	public Document read(String file) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder(true);
		String path = "resources/manifest/manifest.dtd";
		FileInputStream inputStream = new FileInputStream(file);
		return builder.build(inputStream, path);
	}

	public Document read(URL url) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder(true);
		return builder.build(url);
	}
}

package org.lttng.flightbox.xml;

import java.io.FileInputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class ManifestReader {
	
	public Document read(String file, String path) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder(true);
		FileInputStream inputStream = new FileInputStream(file);
		return builder.build(inputStream, path);
	}
}

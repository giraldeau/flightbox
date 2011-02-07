package org.lttng.flightbox.junit.xml;

import java.io.IOException;
import java.net.URL;

import org.jdom.JDOMException;
import org.junit.Test;
import org.lttng.flightbox.xml.ManifestReader;

public class TestManifestReader {

	public static String manifestPath = "/tests/manifest/";
	@Test
	public void testSimpleRead() throws JDOMException, IOException {
		
		String file = System.getenv("project_loc") + manifestPath + "linux_pass_metadata.xml";
		ManifestReader reader = new ManifestReader();
		reader.read(file);
	}
}

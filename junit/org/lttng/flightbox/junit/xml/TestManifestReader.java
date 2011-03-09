package org.lttng.flightbox.junit.xml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.junit.Test;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.xml.ManifestReader;

public class TestManifestReader {

	@Test
	public void testReadAndValidatePass() throws JDOMException, IOException {
		File file = new File(Path.getTestManifestDir(), "linux_pass_metadata.xml");
		ManifestReader reader = new ManifestReader();
		Document doc = reader.read(file.getPath());
		Element root = doc.getRootElement();
		assertTrue(root.getName().compareTo("manifest") == 0);
		
		// list all (channel,event)
		XPath xpath = XPath.newInstance("/manifest/channel/event");
		List<Element> res = (List<Element>) xpath.selectNodes(doc);
		assertEquals(3, res.size());
		
	}
	
	@Test
	public void testReadAndValidateFail() throws IOException {
		File file = new File(Path.getTestManifestDir(), "linux_fail_validation.xml");
		
		Exception eJDOMException = null;
		
		ManifestReader reader = new ManifestReader();
		try {
			reader.read(file.getPath());
		} catch (JDOMException e) {
			eJDOMException = e;
		}
		assertNotNull(eJDOMException);
	}
}

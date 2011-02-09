package org.lttng.flightbox.junit.xml;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.junit.Test;
import org.lttng.flightbox.xml.ManifestReader;

public class TestManifestReader {

	public static String manifestPath = "/tests/manifest/";
	public static String dtdPath = "/manifest/";
	
	@Test
	public void testReadAndValidatePass() throws JDOMException, IOException {
		String file = System.getenv("project_loc") + manifestPath + "linux_pass_metadata.xml";
		String path = System.getenv("project_loc") + dtdPath;
		ManifestReader reader = new ManifestReader();
		Document doc = reader.read(file, path);
		Element root = doc.getRootElement();
		assertTrue(root.getName().compareTo("manifest") == 0);
		
		// list all (channel,event)
		XPath xpath = XPath.newInstance("/manifest/events/event");
		List<Element> res = (List<Element>) xpath.selectNodes(doc);
		assertEquals(res.size(),3);
		
	}
	
	@Test
	public void testReadAndValidateFail() throws IOException {
		String file = System.getenv("project_loc") + manifestPath + "linux_fail_validation.xml";
		String path = System.getenv("project_loc") + dtdPath;
		
		Exception eJDOMException = null;
		
		ManifestReader reader = new ManifestReader();
		try {
			reader.read(file, path);
		} catch (JDOMException e) {
			eJDOMException = e;
		}
		assertNotNull(eJDOMException);
	}
}

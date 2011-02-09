package org.lttng.flightbox.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

public class MarkerSetOperations {

	/*
	 * Verify that inventory is a superset of manifest 
	 * Doesn't validate the hierarchy, ex: the same event could be found in a different channel, which is wrong
	 */
	public static List<Element> containsAll(Document inventory, Document manifest) throws JDOMException {

		ArrayList<Element> missing = new ArrayList<Element>();
		boolean result = false;
		boolean found = false;
		XPath xpath;
		
		xpath = XPath.newInstance("//channel|//event|//field");
		List<Element> allElem = (List<Element>) xpath.selectNodes(manifest);

		for(Element e: allElem) {
			xpath = XPath.newInstance("//" + e.getName() + "[@name='" + e.getAttributeValue("name") + "']");
			Element x = (Element) xpath.selectSingleNode(inventory);
			if (x == null) {
				missing.add(e);
			}
		}
		
		return missing;
	}
	
}

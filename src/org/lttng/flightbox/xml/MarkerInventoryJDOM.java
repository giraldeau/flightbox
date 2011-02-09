package org.lttng.flightbox.xml;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

public class MarkerInventoryJDOM {

	Document inventory;
	static String root = "inventory";
	static String channel = "channel";
	static String event = "event";
	static String field = "field";
	static String name = "name";
	XPath xquery;
	StringBuilder str;
	
	public MarkerInventoryJDOM() {
		inventory = new Document(new Element(root));
		str = new StringBuilder();
	}
	
	public void addChannel(String channelName) throws JDOMException {
		getOrAddChannel(channelName);
	}
	
	public Element getOrAddChannel(String channelName) throws JDOMException {
		Element e = getChannel(channelName);
		if (e == null) {
			e = new Element(channel);
			e.setAttribute(name, channelName);
			inventory.getRootElement().addContent(e);
		}
		return e;
	}
	
	public Element getChannel(String channelName) throws JDOMException {
		String query = buildPath(root,channel) + "[@" + name + "='" + channelName + "']";
		xquery = XPath.newInstance(query);
		Element e = (Element) xquery.selectSingleNode(inventory);
		return e;
	}
	
	public boolean haveChannel(String channelName) throws JDOMException {
		Element e = getChannel(channelName);
		return e != null;
	}
	
	public void addEvent(String channelName, String eventName) throws JDOMException {
		getOrAddEvent(channelName, eventName);
	}
	
	public Element getOrAddEvent(String channelName, String eventName) throws JDOMException {
		Element e = getEvent(channelName, eventName);
		if (e == null) {
			e = new Element(event);
			e.setAttribute(name, eventName);
			Element c = getOrAddChannel(channelName);
			c.addContent(e);
		}
		return e;
	}
	
	public Element getEvent(String channelName, String eventName) throws JDOMException {
		String query = buildPath(root,channel) + "[@" + name + "='" + channelName + "']" +  
		"/" + event + "[@" + name + "='" + eventName + "']";
		xquery = XPath.newInstance(query);
		Element e = (Element) xquery.selectSingleNode(inventory);
		return e;
	}
	
	public boolean haveEvent(String channelName, String eventName) throws JDOMException {
		Element e = getEvent(channelName, eventName);
		return e != null;
	}

	public void addField(String channelName, String eventName, String fieldName) throws JDOMException {
		getOrAddField(channelName, eventName, fieldName);
	}
	
	public Element getOrAddField(String channelName, String eventName, String fieldName) throws JDOMException {
		Element e = getField(channelName, eventName, fieldName);
		if (e == null) {
			e = new Element(field);
			e.setAttribute(name, fieldName);
			Element c = getOrAddEvent(channelName, eventName);
			c.addContent(e);
		}
		return e;
	}
	
	public Element getField(String channelName, String eventName, String fieldName) throws JDOMException {
		String query = buildPath(root,channel) + 
		"[@" + name + "='" + channelName + "']" +  
		"/" + event + "[@" + name + "='" + eventName + "']" + 
		"/" + field + "[@" + name + "='" + fieldName + "']";
		xquery = XPath.newInstance(query);
		Element e = (Element) xquery.selectSingleNode(inventory);
		return e;
	}
	
	public boolean haveField(String channelName, String eventName, String fieldName) throws JDOMException {
		Element e = getField(channelName, eventName, fieldName);
		return e != null;
	}
	
	public String buildPath(String... items) {
		str.setLength(0);
		for (String s: items) {
			str.append("/");
			str.append(s);
		}
		return str.toString();
	}
	
	public Document getInventory() {
		return inventory;
	}
}

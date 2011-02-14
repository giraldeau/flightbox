package org.lttng.flightbox.stub;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.lttng.flightbox.io.TraceReader;

/**
 * This is a fake trace reader that read events from XML file
 * @author francis
 */
public class StubTraceReader extends TraceReader {

	private String tracePath;

	public StubTraceReader(String tracePath) {
		super(tracePath);
	}

	public static JniTrace getJniTrace(String path) {
		SAXBuilder builder = new SAXBuilder(false);
		FileInputStream inputStream = null;
		JniTrace newTrace = null;
		Document doc = null;
		
		try {
			inputStream = new FileInputStream(path);
			doc = builder.build(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (doc != null) {
			newTrace = new StubJniTrace();
			((StubJniTrace)newTrace).setEventsSource(doc);
		}
		return newTrace;
	}
	
	public void loadTrace() {
		trace = getJniTrace(tracePath);
	}
	
	
	
}

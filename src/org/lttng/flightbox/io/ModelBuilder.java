package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.lttng.flightbox.model.SystemModel;

public class ModelBuilder {

	public static void buildFromTrace(String tracePath, SystemModel model) throws JniException {
		// read metadata and statedump
		TraceEventHandlerModelMeta handlerMeta = new TraceEventHandlerModelMeta();
		handlerMeta.setModel(model);
		TraceReader readerMeta = new TraceReader(tracePath);
		readerMeta.register(handlerMeta);
		readerMeta.process();

		// read all trace events
		TraceEventHandlerModel handler = new TraceEventHandlerModel();
		handler.setModel(model);
		TraceReader readerTrace = new TraceReader(tracePath);
		readerTrace.register(handler);
		readerTrace.process();
	} 
}

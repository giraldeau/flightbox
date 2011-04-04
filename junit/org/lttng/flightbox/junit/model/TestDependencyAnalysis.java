package org.lttng.flightbox.junit.model;

import java.io.File;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.dep.BlockingReport;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.io.TraceEventHandlerModel;
import org.lttng.flightbox.io.TraceEventHandlerModelMeta;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class TestDependencyAnalysis {

	@Test
	public void testNanosleep() throws JniException {
		String tracePath = new File(Path.getTraceDir(), "sleep-1x-1sec").getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		model.addTaskListener(listener);

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

		// verify there is /bin/sleep task as child of sleep-1x-1sec
		Task foundTask = model.getLatestTaskByCmdBasename("sleep-1x-1sec");
		StringBuilder str = new StringBuilder();
		BlockingReport.printReport(str, listener.getBlockingItems());
		System.out.println(str.toString());
	}

}

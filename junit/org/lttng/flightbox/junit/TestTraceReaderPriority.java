package org.lttng.flightbox.junit;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.io.TraceEventHandlerDummy;
import org.lttng.flightbox.junit.stub.TestStubs;
import org.lttng.flightbox.stub.StubTraceReader;

public class TestTraceReaderPriority {
	@Test
	public void testTraceHandlerPriority() throws JniException {
		/* the class TraceReader was calling handlers non-deterministically,
		 * such that we rely on a large sample to catch it */
		File file = new File(Path.getTestStubDir(), TestStubs.traceFile);
		StubTraceReader reader = new StubTraceReader(file.getPath());
		ArrayList<TraceEventHandlerDummy> dummies = new ArrayList<TraceEventHandlerDummy>();
		for (int i=0; i<1000; i++) {
			dummies.add(new TraceEventHandlerDummy(i));
			if (i>0)
				dummies.get(i).setBuddy(dummies.get(i-1));
		}
		Collections.shuffle(dummies);
		for (TraceEventHandlerDummy handler: dummies) {
			reader.register(handler);
		}
		reader.process();
		assertFalse(reader.isCancel());
	}
}

package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.io.TraceEventHandlerModel;
import org.lttng.flightbox.io.TraceEventHandlerModelMeta;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.DiskFile;
import org.lttng.flightbox.model.FileDescriptor;
import org.lttng.flightbox.model.SymbolTable;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class TestModelHandler {

	static double nanosec = 1000000000;
	static double p = 100000000.0;

	@Test
	public void testSyscallTable() throws JniException {
		String tracePath = new File(Path.getTraceDir(), "sleep-1x-1sec").getPath();
		SystemModel model = new SystemModel();

		// read metadata and statedump
		TraceEventHandlerModelMeta handlerMeta = new TraceEventHandlerModelMeta();
		handlerMeta.setModel(model);
		TraceReader readerMeta = new TraceReader(tracePath);
		readerMeta.register(handlerMeta);
		readerMeta.process();

		SymbolTable syscalls = model.getSyscallTable();
		SymbolTable interrupts = model.getInterruptTable();
		SymbolTable softirq = model.getSoftIRQTable();

		// FIXME: will this work on another architecture than x86?
		assertTrue(syscalls.getMap().size() > 255); // was 336

		assertEquals(256, interrupts.getMap().size()); // is always 256

		assertTrue(softirq.getMap().size() > 30); // was 32
	}

	@Test
	public void testModelHandler() throws JniException {
		/*
		 * This test is based on the fact that the script sleep-1x-1sec
		 * spawn only one child /bin/sleep
		 */
		String tracePath = new File(Path.getTraceDir(), "sleep-1x-1sec").getPath();
		SystemModel model = new SystemModel();

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
		assertNotNull(foundTask);
		SortedSet<Task> children = foundTask.getChildren();
		Task task = children.first();
		String cmd = task.getCmd();
		assertEquals("/bin/sleep", cmd);
		double duration = (task.getEndTime() - task.getStartTime());
		assertEquals(duration, nanosec, p);
		assertEquals(false, foundTask.isKernelThread());

		// verify swapper is process 0 and is a kernel thread
		Task swapper = model.getLatestTaskByCmdBasename("swapper");
		assertNotNull(swapper);
		assertEquals(0, swapper.getProcessId());
		assertEquals(true, swapper.isKernelThread());

	}

	@Test
	public void testModelFileDescriptor() throws JniException {
		String tracePath = new File(Path.getTraceDir(), "cat-to-null").getPath();
		SystemModel model = new SystemModel();

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
		Task foundTask = model.getLatestTaskByCmdBasename("cat-to-null");
		assertNotNull(foundTask);
		SortedSet<Task> children = foundTask.getChildren();
		assertEquals(2, children.size());

		Task first = children.first();
		Task last = children.last();
		assertEquals("/bin/cat", first.getCmd());
		assertEquals("/bin/cat", last.getCmd());

		HashMap<Integer, TreeSet<FileDescriptor>> fds = first.getFileDescriptors();
		TreeSet<FileDescriptor> set = fds.get(3);
		DiskFile f = (DiskFile) set.last();
		assertEquals("cat-to-null", new File(f.getFilename()).getName());
		assertFalse(f.isOpen());
		assertFalse(f.isError());

		fds = last.getFileDescriptors();
		set = fds.get(-2);
		f = (DiskFile) set.first();
		assertEquals("77d2c0533bea11686a892bcb34697292", f.getFilename());
		assertFalse(f.isOpen());
		assertTrue(f.isError());

	}

}

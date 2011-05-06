package org.lttng.flightbox.junit.model;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.io.TraceEventHandlerModel;
import org.lttng.flightbox.io.TraceEventHandlerModelMeta;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.FileDescriptor;
import org.lttng.flightbox.model.SocketInet;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class TestModelSocket {

	@Test
	public void testRetreiveSocket() throws JniException {
		String tracePath = new File(Path.getTraceDir(), "tcp-simple").getPath();
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

		// the latest netcat is the client
		TreeSet<Task> tasks = model.getTaskByCmd("netcat", true);
		assertEquals(2, tasks.size());

		Task server = tasks.first();
		Task client = tasks.last();

		SocketInet clientSocket = findSocket(client);
		SocketInet serverSocket = findSocket(server);

		assertNotNull(clientSocket);
		assertNotNull(serverSocket);

		assertEquals(8765, clientSocket.getDstPort());
		assertEquals(8765, serverSocket.getSrcPort());
		
		assertTrue(clientSocket.isComplementary(serverSocket));

		assertFalse(clientSocket.isOpen());
		assertFalse(serverSocket.isOpen());

	}

	/** 
	 * returns the first defined socket of the task
	 * @param task
	 * @return
	 */
	public SocketInet findSocket(Task task) {
		HashMap<Integer, TreeSet<FileDescriptor>> fds = task.getFileDescriptors();
		SocketInet sock = null;
		for (Integer i : fds.keySet()) {
			FileDescriptor last = fds.get(i).last();
			if (last instanceof SocketInet) {
				SocketInet s = (SocketInet) last;
				if (s.isSet()) {
					sock = s;
					break;
				}
			}
		}
		return sock;
	}

}

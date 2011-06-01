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
import org.lttng.flightbox.io.ModelBuilder;
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
		String tracePath = new File(Path.getTraceDir(), "rpc-sleep-100ms").getPath();
		SystemModel model = new SystemModel();

		ModelBuilder.buildFromTrace(tracePath, model);

		// the last netcat is the client
		TreeSet<Task> tasksServer = model.getTaskByCmd("srvhog", true);
		assertEquals(1, tasksServer.size());

		TreeSet<Task> tasksClient = model.getTaskByCmd("clihog", true);
		assertEquals(1, tasksClient.size());

		Task server = tasksServer.first();
		Task client = tasksClient.first();

		SocketInet clientSocket = findSocket(client);
		SocketInet serverSocket = findSocket(server);

		assertNotNull(clientSocket);
		assertNotNull(serverSocket);

		assertEquals(9876, clientSocket.getDstPort());
		assertEquals(9876, serverSocket.getSrcPort());
		
		assertTrue(clientSocket.isComplementary(serverSocket));

		assertFalse(clientSocket.isOpen());
		assertFalse(serverSocket.isOpen());

		assertTrue(clientSocket.isClient());
		assertFalse(serverSocket.isClient());
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

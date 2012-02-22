package org.lttng.flightbox.junit.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lttng.flightbox.model.DiskFile;
import org.lttng.flightbox.model.SocketInet;

public class TestFileDescriptors {

	/*
	 * suite of tests for file descriptor objects
	 */
	
	@Test
	public void testCreateSimpleDiskFile() {
		DiskFile fd1 = new DiskFile();
		fd1.setFd(0);
		DiskFile fd2 = new DiskFile();
		fd2.setFd(fd1.getFd());
		assertEquals(fd1, fd2);
	}
	
	@Test
	public void testCreateSimpleSocketInet() {
		long clientAddr = 1212;
		int clientPort = 12121;
		long serverAddr = 9898;
		int serverPort = 98989;
		
		SocketInet client = new SocketInet();
		client.setFd(3);
		client.setDstAddr(serverAddr);
		client.setDstPort(serverPort);
		client.setSrcAddr(clientAddr);
		client.setSrcPort(clientPort);
		
		SocketInet server = new SocketInet();
		server.setFd(4);
		server.setDstAddr(clientAddr);
		server.setDstPort(clientPort);
		server.setSrcAddr(serverAddr);
		server.setSrcPort(serverPort);
		assertTrue(client.isSet());
		assertTrue(server.isSet());
		assertTrue(client.isComplementary(server));
	}
}

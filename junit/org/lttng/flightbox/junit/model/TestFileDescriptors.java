package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.TreeSet;

import org.junit.Test;
import org.lttng.flightbox.model.FileDescriptor;
import org.lttng.flightbox.model.FileDescriptorSet;
import org.lttng.flightbox.model.RegularFile;
import org.lttng.flightbox.model.SocketInet;

public class TestFileDescriptors {

	/*
	 * suite of tests for file descriptor objects
	 */
	
	@Test
	public void testCreateSimpleDiskFile() {
		RegularFile fd1 = new RegularFile();
		fd1.setFd(0);
		RegularFile fd2 = new RegularFile();
		fd2.setFd(fd1.getFd());
		assertEquals(fd1, fd2);
	}
	
	@Test
	public void testCreateSimpleSocketInet() {
		long clientAddr = 1212;
		int clientPort = 12121;
		long serverAddr = 9898;
		int serverPort = 98989;
		
		SocketInet client = new SocketInet(clientAddr, clientPort, serverAddr, serverPort);
		client.setFd(3);
		
		SocketInet server = new SocketInet(serverAddr, serverPort, clientAddr, clientPort);
		server.setFd(4);
		assertTrue(client.getIp().isSet());
		assertTrue(server.getIp().isSet());
		assertTrue(client.isConnected(server));
	}
	public <T extends FileDescriptor> TreeSet<T> makeFDSet(Class<T> type, int start) {
		TreeSet<T> set = new TreeSet<T>();
		int i, j, k = start;
		int nb = 3;
		
		for (i=0; i<=nb; i++) {
			for (j=0; j<i; j++) {
				T fd = null;
				try {
					fd = type.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
					throw new RuntimeException();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					throw new RuntimeException();
				}
				fd.setFd(j);
				fd.setStartTime(k++);
				set.add(fd);
			}
		}
		return set;
	}
	
	@Test
	public void testFileDescriptorSet() {
		TreeSet<RegularFile> regFileSet = makeFDSet(RegularFile.class, 0);
		TreeSet<SocketInet> socketInetSet = makeFDSet(SocketInet.class, regFileSet.size());
		FileDescriptorSet fdSet = new FileDescriptorSet();
		fdSet.addAll(regFileSet);
		fdSet.addAll(socketInetSet);
		assertEquals(regFileSet.size() + socketInetSet.size(), fdSet.historySize());
		for (RegularFile obj: regFileSet) {
			fdSet.remove(obj);
		}
		for (SocketInet obj: socketInetSet) {
			fdSet.remove(obj);
		}
		assertEquals(0, fdSet.historySize());
	}
}

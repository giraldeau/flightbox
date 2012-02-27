package org.lttng.flightbox.model;

import java.util.Set;

public class FileDescriptorSet {

	private final IdMap<SocketInet> sockets;
	private final IdMap<RegularFile> regFiles;
	private final IdMap<FileDescriptor> fds;
	
	public FileDescriptorSet() {
		sockets = new IdMap<SocketInet>();
		sockets.setProvider(new IdProvider<SocketInet>() {
			@Override
			public int getId(SocketInet obj) {
				return obj.getFd();
			}
		});
		
		regFiles = new IdMap<RegularFile>();
		regFiles.setProvider(new IdProvider<RegularFile>() {
			@Override
			public int getId(RegularFile obj) {
				return obj.getFd();
			}
		});
		
		fds = new IdMap<FileDescriptor>();
		fds.setProvider(new IdProvider<FileDescriptor>() {
			@Override
			public int getId(FileDescriptor obj) {
				return obj.getFd();
			}
		});
	}
	
	/* if a duplicate exists (fd,starTime), the fd is *not* added
	 * and no error is thrown. This is acceptable on per-process basis
	 * because no such duplicate can exists for a single process */
	public <T extends FileDescriptor> void add(T fd) {
		if (fd instanceof RegularFile) {
			regFiles.add((RegularFile) fd);
		} else if (fd instanceof SocketInet) {
			sockets.add((SocketInet) fd);
		}
		fds.add(fd);
	}
	
	public <T extends FileDescriptor> void addAll(Set<T> set) {
		for (T obj: set) {
			add(obj);
		}
	}
	
	public int size() {
		return fds.size();
	}
	
	public int historySize() {
		return fds.historySize();
	}
	
	public String toString() {
		return fds.toString();
	}
}

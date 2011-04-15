package org.lttng.flightbox.model;

public class FileDescriptor extends SystemResource {

	private int fd;

	public void setFd(int fd) {
		this.fd = fd;
	}

	public int getFd() {
		return fd;
	}

}

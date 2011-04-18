package org.lttng.flightbox.model;

public class DiskFile extends FileDescriptor {

	String filename;

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return this.filename;
	}

}

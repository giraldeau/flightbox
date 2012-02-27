package org.lttng.flightbox.model;

public class RegularFile extends FileDescriptor {

	String filename;

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return this.filename;
	}

	@Override
	public String toString() {
		return String.format("[%d,%s]", getFd(), getFilename()); 
	}

}

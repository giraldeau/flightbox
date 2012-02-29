package org.lttng.flightbox.model;

public class FileDescriptorIdProvider <T extends FileDescriptor> implements IdProvider<T> {
	
	@Override
	public int getId(T obj) {
		return obj.getFd();
	}
}

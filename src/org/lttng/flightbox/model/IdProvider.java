package org.lttng.flightbox.model;

public interface IdProvider <T> {

	public int getId(T obj);
	
}

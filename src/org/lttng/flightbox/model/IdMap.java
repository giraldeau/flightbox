package org.lttng.flightbox.model;

import java.util.HashMap;
import java.util.TreeSet;

public class IdMap <T> {

	private final HashMap<Integer, TreeSet<T>> fds;
	private IdProvider<T> provider;
	
	public IdMap() {
		fds = new HashMap<Integer, TreeSet<T>>();
	}
	
	public T getLatest(int id) {
		TreeSet<T> set = fds.get(id);
		if (set == null || set.size() == 0)
			return null;
		return set.last();
	}

	public boolean add(T fd) {
		if (provider == null)
			throw new Error("provider is null");
		int id = provider.getId(fd);
		TreeSet<T> set = fds.get(id);
		if (set == null) {
			set = new TreeSet<T>();
			fds.put(id, set);
		}
		return set.add(fd);
	}
	
	public HashMap<Integer, TreeSet<T>> getMap() {
		return fds;
	}

	public void setProvider(IdProvider<T> provider) {
		this.provider = provider;
	}

	public IdProvider<T> getProvider() {
		return provider;
	}
	
	
	
}

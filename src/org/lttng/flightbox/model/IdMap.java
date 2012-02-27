package org.lttng.flightbox.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * IdMap is a generic HashMap to use with objects having integer id. The id 
 * can be reused and the complete history is kept. The object must 
 * implements Comparable interface for the history. The IdProvider aim is to 
 * wrap the method of the object returning it's actual id, and allow lose coupling.
 * 
 * @author francis
 *
 * @param <T>
 */
public class IdMap <T extends Comparable<?>> {

	private final HashMap<Integer, TreeSet<T>> map;
	private IdProvider<T> provider;
	
	public IdMap() {
		map = new HashMap<Integer, TreeSet<T>>();
	}
	
	public T getLatest(int id) {
		TreeSet<T> set = map.get(id);
		if (set == null || set.size() == 0)
			return null;
		return set.last();
	}

	public boolean add(T obj) {
		if (provider == null)
			throw new Error("provider is null");
		int id = provider.getId(obj);
		TreeSet<T> set = map.get(id);
		if (set == null) {
			set = new TreeSet<T>();
			map.put(id, set);
		}
		return set.add(obj);
	}
	
	public HashMap<Integer, TreeSet<T>> getMap() {
		return map;
	}

	public void setProvider(IdProvider<T> provider) {
		this.provider = provider;
	}

	public IdProvider<T> getProvider() {
		return provider;
	}

	public void addAll(Set<? extends T> set) {
		for (T obj: set) {
			add(obj);
		}
	}
	
	public int size() {
		return map.size();
	}
	public int historySize() {
		int historySize = 0;
		for (Set<T> set: map.values()) {
			historySize += set.size();
		}
		return historySize;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(map);
		return str.toString();
	}
}

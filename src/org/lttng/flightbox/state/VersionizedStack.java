package org.lttng.flightbox.state;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

public class VersionizedStack<T> {

	public class Item<M> implements Comparable<Item<M>> {
		public M content;
		public Long id;
		
		@Override
		public int compareTo(Item<M> other) {
			return id.compareTo(other.id);
		}
		
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			if (content != null)
				builder.append(content.toString());
			builder.append(",");
			if (id != null)
				builder.append(id.toString());
			builder.append(")");
			return builder.toString();
		}
		
	}

	LinkedList<Item<T>> itemStack;
	TreeSet<Item<T>> itemSet;
	Item<T> item;
	Set<T> symbols;
	
	public VersionizedStack () {
		itemStack = new LinkedList<Item<T>>();
		itemSet = new TreeSet<Item<T>>();
		item = new Item<T>();
		symbols = new HashSet<T>();
	}
	
	public void push(T obj, Long i) {
		Item<T> item = new Item<T>();
		item.id = i;
		item.content = obj;
		itemSet.add(item);
		itemStack.add(item);
	}
	
	public T peek(Long i) {
		item.id = i;
		Item<T> x = itemSet.floor(item);
		if (x != null)
			return x.content;
		return null;
	}
	
	public T peek() {
		if (itemStack.isEmpty())
			return null;
		Item<T> x = itemStack.getLast();
		if (x != null)
			return x.content;
		return null;
	}
	
	public T pop(Long i) {
		Item<T> newItem = new Item<T>();
		newItem.id = i;
		Item<T> ret = null;
		if (itemStack.size() == 1) {
			ret = itemStack.removeLast();
		} else if (itemStack.size() > 1) {
			ret = itemStack.removeLast();
			Item<T> x = itemStack.getLast();
			newItem.content = x.content;
		}
		itemSet.add(newItem);
		if (ret != null)
			return ret.content;
		return null;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(itemStack.toString());
		builder.append(itemSet.toString());
		return builder.toString();
	}

	public Object size() {
		return itemSet.size();
	}

	public Set<T> getSymbols() {
		return symbols;
	}
	public void setSymbols(Set<T> obj) {
		this.symbols = obj;
	}
}
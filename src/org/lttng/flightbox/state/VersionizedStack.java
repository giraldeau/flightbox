package org.lttng.flightbox.state;

import java.util.HashSet;
import java.util.Iterator;
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
	Interval interval;
	Set<T> symbols;
	private boolean updateSymbols;
	private boolean isInitialFixed;
	
	
	public VersionizedStack () {
		itemStack = new LinkedList<Item<T>>();
		itemSet = new TreeSet<Item<T>>();
		item = new Item<T>();
		symbols = new HashSet<T>();
		interval = new Interval();
		isInitialFixed = false;
	}
	
	public void push(T obj, Long i) {
		Item<T> item = new Item<T>();
		item.id = i;
		item.content = obj;
		itemSet.add(item);
		itemStack.add(item);
		updateSymbols = true;
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
	
	public T pop(T obj, Long i) {
		if (itemStack.isEmpty() && !isInitialFixed){
			// fix initial state
			Item<T> item = new Item<T>();
			item.content = obj;
			item.id = 0L;
			itemStack.push(item);
			itemSet.add(item);
			for(Item<T> x: itemSet) {
				if (x.content == null) {
					x.content = obj;
				}
			}
		}
		return pop(i);
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
		if (updateSymbols) {
			symbols.clear();
			for (Item<T> i: itemSet) {
				if (!symbols.contains(i.content)) {
					symbols.add(i.content);
				}
			}
		}
		return symbols;
	}
	public void setSymbols(Set<T> obj) {
		this.symbols = obj;
	}
	public Set<Item<T>> getHistory() {
		return this.itemSet;
	}

	protected class StackIterator implements Iterator<Interval> {
		int counter;
		Iterator<Item<T>> it;
		Item<T> curr, next;
		Interval interval;
		public StackIterator() {
			interval = new Interval();
			counter = 0;
			it = itemSet.iterator();
			if (it.hasNext()) {
				curr = it.next();
			}
			next = null;
		}
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}
		@Override
		public Interval next() {
			next = it.next();
			interval.t1 = curr.id;
			interval.t2 = next.id;
			interval.content = curr.content;
			curr = next;
			return interval;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public Iterator<Interval> iterator() {
		return new StackIterator(); 
	}
}
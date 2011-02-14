package org.lttng.flightbox.state;

import org.lttng.flightbox.state.Symbol;

/**
 * Representation of states for hedge automata
 * @author francis
 */

public class Event implements Symbol {

	/** unique id */
	private int id;
	
	/** counter for next available id */
	private static int counter = 0;
	
	String label;
	
	/**
	 * Constructs a new hedge state, and set the id to the next available
	 */
	public Event(){
		// FIXME: is this thread safe?
		this(counter++);
	}
	/**
	 * Constructs a new hedge state
	 * @param id id of the hedge state
	 */	
	public Event(int id){
		this.id = id;
	}

	public Event(String s){
		this();
		setLabel(s);
	}
	
	/**
	 * Returns the id of this hedge state
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of this hedge state
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
	public String getLabel(){
		return label;
	}
	
	public boolean equals(Object obj){
		if (obj instanceof Event){
			Event s = (Event)obj;
			return this.id == s.id;
		}
		return false;
	}
	
	public int hashCode(){
		return this.id * 3;
	}
	
	public String toString(){
		if (label != null){
			return label;
		} else {
			return Integer.toString(id);
		}
	}
}

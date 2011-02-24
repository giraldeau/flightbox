package org.lttng.flightbox.automaton;

/**
 * Representation of transitions of automaton
 * @author francis
 */

public class Transition {

	/** symbol of this transition */
	private Event symbol;
	
	/** destination state */
	private State destState;

	/** epsilon symbol */
	private static Event eps = new Event();

	/**
	 * Construct a new transition, with epsilon symbol
	 * @param destState destination state
	 */
	public Transition(State destState){
		this(eps, destState);
	}
	
	/**
	 * Construct a new transition
	 * @param symbol
	 * @param destState
	 */
	public Transition(Event symbol, State destState){
		this.symbol = symbol;
		this.destState = destState;
	}
	
	/**
	 * Returns the symbol of this transition
	 * @return symbol
	 */
	public Event getSymbol() {
		return symbol;
	}
	
	/**
	 * Set the symbol of this transition
	 * @param symbol
	 */
	public void setSymbol(Event symbol) {
		this.symbol = symbol;
	}
	
	/**
	 * Returns the destination state of this transition
	 * @return destState
	 */
	public State getDestState() {
		return destState;
	}
	
	/**
	 * Set the destination state of this transition
	 * @param destState
	 */
	public void setDestState(State destState) {
		this.destState = destState;
	}
	
	/**
	 * Returns the epsilon status
	 * @return if true, this transition is epsilon
	 */
	public boolean isEps() {
		return this.symbol == Transition.eps;
	}
	
	/**
	 * Set this transition as epsilon
	 */
	public void setEps() {
		this.symbol = Transition.eps;
	}
	
	/**
	 * Test if this transition accept the symbol
	 * @param input
	 * @return if true, this transition accept the input symbol 
	 */
	public boolean acceptSymbol(Event input) {
		return (symbol.getId() == input.getId());
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj instanceof Transition){
			Transition t = (Transition)obj;
			return symbol.equals(t.symbol) && 
					destState.equals(t.destState);
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return symbol.getId() * 2 + destState.getId() * 3;
	}
}

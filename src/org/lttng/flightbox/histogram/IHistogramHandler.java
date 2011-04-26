package org.lttng.flightbox.histogram;

import statehistory.StateHistorySystem;
import statehistory.common.AttributeNotFoundException;

public interface IHistogramHandler {

	public int[] getSamples() throws AttributeNotFoundException;
	public void setNbSamples(int s);
	public void setStateHistorySystem(StateHistorySystem shs);

}

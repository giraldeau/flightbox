package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Processor.ProcessorState;

public interface IProcessorListener {

	public void stateChange(Processor processor, ProcessorState nextState);
	public void lowPowerModeChange(Processor processor, boolean nextlowPowerMode);
	
}

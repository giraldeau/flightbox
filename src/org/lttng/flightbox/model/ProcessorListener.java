package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Processor.ProcessorState;

public abstract class ProcessorListener implements IProcessorListener {

	@Override
	public void stateChange(Processor processor, ProcessorState nextState) {
	}

	@Override
	public void lowPowerModeChange(Processor processor, boolean nextlowPowerMode) {
	}

}

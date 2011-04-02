package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.lttng.flightbox.model.Processor;
import org.lttng.flightbox.model.ProcessorListener;
import org.lttng.flightbox.model.SystemModel;

public class TestSystemModel {

	class MyListener extends ProcessorListener {
		public boolean mode = false;
		@Override
		public void lowPowerModeChange(Processor processor, boolean nextlowPowerMode) {
			mode = nextlowPowerMode;
		}
	}

	@Test
	public void testSystemListeners() {
		MyListener listener = new MyListener();
		SystemModel model = new SystemModel();
		model.initProcessors(2);
		model.addProcessorListener(listener);
		model.getProcessors().get(1).setLowPowerMode(true);
		assertEquals(true, listener.mode);
	}
}

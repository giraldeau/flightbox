package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.lttng.flightbox.history.KernelSystemHistoryManager;
import org.lttng.flightbox.io.TimeKeeper;
import org.lttng.flightbox.model.KernelSystem;
import org.lttng.flightbox.model.Processor;

import statehistory.StateHistorySystem;
import statehistory.common.AttributeNotFoundException;

public class TestKernelSystemHistoryManager {

	static File temp = null;
	
	@BeforeClass
	public static void setup() throws IOException {
		temp = File.createTempFile("history-test",".shs");
		temp.deleteOnExit();
	}
	
	@Test
	public void testInitHistory() throws IOException, AttributeNotFoundException {
		long start = 10;
		long t1 = 10;
		long t2 = 20;
		
		KernelSystemHistoryManager manager = new KernelSystemHistoryManager();
		manager.initHistory(temp.getPath(), start);
		KernelSystem model = new KernelSystem();
		manager.registerModel(model);
		
		model.initProcessors(2);
		Processor p1 = model.getProcessors().get(1);
		TimeKeeper.getInstance().setCurrentTime(t1);
		p1.setLowPowerMode(true);
		TimeKeeper.getInstance().setCurrentTime(t2);
		p1.setLowPowerMode(false);

		/* FIXME: bug with reading a state history file */
		/*
		manager.finalize();
		try {
			StateHistorySystem shs = new StateHistorySystem(temp.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/

		StateHistorySystem shs = manager.getSHT();
		int val1 = shs.getSingleStateValueInt(t1 + 1, "Processor", "lowPowerMode", "1");
		int val2 = shs.getSingleStateValueInt(t2 + 1, "Processor", "lowPowerMode", "1");
		assertEquals(1, val1);
		assertEquals(0, val2);
		
	}
	
}

package org.lttng.flightbox.junit;

import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.state.history.StateEventHandler;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestStateHistory {

	TmfExperiment<LttngEvent> fExperiment = null;
	static TmfTimeRange allRange;
	
	@BeforeClass
	public static void setupClass() {
	    // Create a new time range from -infinity to +infinity
	    //	That way, we will get "everything" in the trace
	    LttngTimestamp ts1 = new LttngTimestamp(Long.MIN_VALUE);
	    LttngTimestamp ts2 = new LttngTimestamp(Long.MAX_VALUE);
	    allRange = new TmfTimeRange(ts1, ts2);
	}
	
	public void setupTrace(String trace_path) throws Exception {
		ITmfTrace[] traces = new ITmfTrace[1];
		traces[0] = new LTTngTrace(trace_path);
    	// Create our new experiment
        fExperiment = new TmfExperiment<LttngEvent>(LttngEvent.class, "Headless", traces);
	}
	
	@Test
	public void testFillStateHistory() throws Exception {
		setupTrace("/home/francis/workspace/traces/trace-sleep1sec");
		StateEventHandler handler = new StateEventHandler(LttngEvent.class, allRange, Integer.MAX_VALUE);

		fExperiment.sendRequest(handler);
		
		// FIXME: this is a ugly hack because otherwise the sendRequest doesn't block
		//fExperiment.wait();
		while(!handler.isDone()) {
			Thread.sleep(100);
		}
		
    	System.out.println("Nb events : " + handler.getNbEvent());
    	System.out.println("Events per CPU" + handler.getNbEventPerCPU());
    	System.out.println("Marker founds: ");
    	for (String s: handler.getMarkerMap().keySet()) {
    		String spc = new String();
    		for(int i=0; i<40 - s.length();i++) {
    			spc += " ";
    		}
			System.out.println(s + spc + handler.getMarkerMap().get(s));
    	}
		
		System.out.println("test");
		assertTrue(true);
	}
	
}

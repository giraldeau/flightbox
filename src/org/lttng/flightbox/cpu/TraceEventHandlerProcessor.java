package org.lttng.flightbox.cpu;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.model.KernelSystem;
import org.lttng.flightbox.model.Processor;

public class TraceEventHandlerProcessor extends TraceEventHandlerBase {

	private KernelSystem systemModel;

	public TraceEventHandlerProcessor() {
		super();
		hooks.add(new TraceHook("pm", "idle_entry"));
		hooks.add(new TraceHook("pm", "idle_exit"));
	}
	
	public void handleInit(TraceReader reader, JniTrace trace) {
	
	}
	
	public void handle_pm_idle_entry(TraceReader reader, JniEvent event) {
		Processor processor = systemModel.getProcessors().get((int)event.getParentTracefile().getCpuNumber());
		processor.setLowPowerMode(false);
	}
	
	public void handle_pm_idle_exit(TraceReader reader, JniEvent event) {
		Processor processor = systemModel.getProcessors().get((int)event.getParentTracefile().getCpuNumber());
		processor.setLowPowerMode(true);
	}
	
	@Override
	public void handleComplete(TraceReader reader) {
		
	}
}

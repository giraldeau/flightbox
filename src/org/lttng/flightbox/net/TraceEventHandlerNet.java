package org.lttng.flightbox.net;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.cpu.KernelProcess;
import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;

public class TraceEventHandlerNet extends TraceEventHandlerBase {

	public TraceEventHandlerNet() {
		super();
		hooks.add(new TraceHook("net", "dev_xmit_extended"));
		hooks.add(new TraceHook("net", "tcpv4_rcv_extended"));
	}
	
	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {

	}

	public void handle_net_dev_xmit_extended(TraceReader reader, JniEvent event) {
		debugEvent(reader, event);
	}
	
	public void handle_net_tcpv4_rcv_extended(TraceReader reader, JniEvent event) {
		debugEvent(reader, event);	
	}

	public void debugEvent(TraceReader reader, JniEvent event) {
		String eventName = event.getMarkersMap().get(event.getEventMarkerId()).getName();

		Long cpu = event.getParentTracefile().getCpuNumber();
		double eventTs = (double) event.getEventTime().getTime();
		TraceEventHandlerProcess processHandler = (TraceEventHandlerProcess) reader.getHandler(TraceEventHandlerProcess.class);
		KernelProcess proc = processHandler.getCurrentProcess(cpu);
		System.out.println("ts=" + eventTs + " cpu=" + cpu + " pid=" + proc.getCmd() + " "  + eventName);
	}

	@Override
	public void handleComplete(TraceReader reader) {
		
	}

}

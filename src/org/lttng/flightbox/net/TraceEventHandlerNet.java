package org.lttng.flightbox.net;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.cpu.KernelProcess;
import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
import org.lttng.flightbox.io.TraceEventHandler;
import org.lttng.flightbox.io.TraceReader;

public class TraceEventHandlerNet implements TraceEventHandler {

	private TraceReader traceReader;

	@Override
	public void handleInit(JniTrace trace) {
		
	}

	@Override
	public void handleEvent(JniEvent event) {
		String eventName = event.getMarkersMap().get(event.getEventMarkerId()).getName();
		Long cpu = event.getParentTracefile().getCpuNumber();
		double eventTs = (double) event.getEventTime().getTime();
		
		if (eventName.compareTo("dev_xmit_extended") == 0){
		} else if (eventName.compareTo("tcpv4_rcv_extended") == 0) {
			// {saddr, syn, ack, ack_seq, ihl, fin, daddr, dest, source, rst seq, tot_len, skb, doff}
		}
		TraceEventHandlerProcess processHandler = (TraceEventHandlerProcess) traceReader.getHandler(TraceEventHandlerProcess.class);
		KernelProcess proc = processHandler.getCurrentProcess(cpu);
		//System.out.println("ts=" + eventTs + " cpu=" + cpu + " pid=" + proc.getCmd() + " " + eventName + " " + event.parseAllFields());
		System.out.println("ts=" + eventTs + " cpu=" + cpu + " pid=" + proc.getCmd() + " " + eventName);

	}

	@Override
	public void handleComplete() {
		
	}

	@Override
	public void setTraceReader(TraceReader traceReader) {
		this.traceReader = traceReader;
	}
}

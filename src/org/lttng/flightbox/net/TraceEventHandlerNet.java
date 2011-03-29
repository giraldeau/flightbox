package org.lttng.flightbox.net;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.model.KernelTask;

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
		TraceEventHandlerProcess processHandler = (TraceEventHandlerProcess) reader.getHandler(TraceEventHandlerProcess.class);
		/* we can't proceed without information about process */
		if (processHandler == null)
			return;
		
		double eventTs = (double) event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		KernelTask proc = processHandler.getCurrentProcess(cpu);
		Long network_protocol = (Long) event.parseFieldByName("network_protocol");
		Long transport_protocol = (Long) event.parseFieldByName("transport_protocol");
		Long saddr = (Long) event.parseFieldByName("saddr");
		Long daddr = (Long) event.parseFieldByName("daddr");
		Long tot_len = (Long) event.parseFieldByName("tot_len");
		Long ihl = (Long) event.parseFieldByName("ihl");
		Long source = (Long) event.parseFieldByName("source");
		Long dest = (Long) event.parseFieldByName("dest");
		Long seq = (Long) event.parseFieldByName("seq");
		Long ack_seq = (Long) event.parseFieldByName("ack_seq");
		Long doff = (Long) event.parseFieldByName("doff");
		Long ack = (Long) event.parseFieldByName("ack");
		Long rst = (Long) event.parseFieldByName("rst");
		Long syn = (Long) event.parseFieldByName("syn");
		Long fin = (Long) event.parseFieldByName("fin");
		System.out.println(proc.toString() + " xmit " + daddr + ":" + dest + " syn=" + syn + " ack=" + ack + " fin=" + fin);
	}
	
	public void handle_net_tcpv4_rcv_extended(TraceReader reader, JniEvent event) {
		TraceEventHandlerProcess processHandler = (TraceEventHandlerProcess) reader.getHandler(TraceEventHandlerProcess.class);
		/* we can't proceed without information about process */
		if (processHandler == null)
			return;
		
		double eventTs = (double) event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		KernelTask proc = processHandler.getCurrentProcess(cpu);
		Long saddr = (Long) event.parseFieldByName("saddr");
		Long daddr = (Long) event.parseFieldByName("daddr");
		Long tot_len = (Long) event.parseFieldByName("tot_len");
		Long ihl = (Long) event.parseFieldByName("ihl");
		Long source = (Long) event.parseFieldByName("source");
		Long dest = (Long) event.parseFieldByName("dest");
		Long seq = (Long) event.parseFieldByName("seq");
		Long ack_seq = (Long) event.parseFieldByName("ack_seq");
		Long doff = (Long) event.parseFieldByName("doff");
		Long ack = (Long) event.parseFieldByName("ack");
		Long rst = (Long) event.parseFieldByName("rst");
		Long syn = (Long) event.parseFieldByName("syn");
		Long fin = (Long) event.parseFieldByName("fin");
		System.out.println(proc.toString() + " recv " + daddr + ":" + dest + " syn=" + syn + " ack=" + ack + " fin=" + fin);
	}

	@Override
	public void handleComplete(TraceReader reader) {
		
	}

}

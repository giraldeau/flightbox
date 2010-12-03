package org.lttng.flightbox.cpu;

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.CpuUsageStats;
import org.lttng.flightbox.GlobalState.KernelMode;
import org.lttng.flightbox.TimeStats;
import org.lttng.flightbox.io.EventData;
import org.lttng.flightbox.io.TraceEventHandler;

public class TraceEventHandlerCpu implements TraceEventHandler {
	
	int count; 
	JniTrace trace;

	HashMap<Long, EventData> cpuHistory;
	CpuUsageStats cpuStats;
	private int numCpu;
	private double start;
	private double end;
	
	@Override
	public void handleInit(JniTrace trace) {
		this.trace = trace;
		count = 0;
		cpuHistory = new HashMap<Long, EventData>();
		cpuStats = new CpuUsageStats();
		numCpu = trace.getCpuNumber();
		start = (double) trace.getStartTime().getTime();
		end = (double) trace.getEndTime().getTime();
		cpuStats.setStart(start);
		cpuStats.setEnd(end);
	}
	
	@Override
	public void handleEvent(JniEvent event) {
		String eventName = event.getMarkersMap().get(event.getEventMarkerId()).getName();
		
		if (eventName.compareTo("sched_schedule") != 0){
			return;
		}

		count++;
		Long cpu = event.getParentTracefile().getCpuNumber();
		long eventTs = event.getEventTime().getTime();
		Long prev_pid = (Long) event.parseFieldByName("prev_pid");
		Long next_pid = (Long) event.parseFieldByName("next_pid");
		
		double t = 0;
		if (cpuHistory.containsKey(cpu)) { // we have a previous event
			t = cpuHistory.get(cpu).getTime();
		} else { // first event for this CPU
			t = start; 
			cpuHistory.put(cpu, new EventData());
		}
		if (prev_pid > 0) {
			cpuStats.addInterval(t, eventTs, cpu, KernelMode.USER);
		}
		// update history to keep track of previous event
		cpuHistory.get(cpu).update(event);
	}

	@Override
	public void handleComplete() {
		// finish intervals until trace end
		EventData event;
		for(Long cpu: cpuHistory.keySet()) {
			// last event for this CPU
			event = cpuHistory.get(cpu);
			long eventTs = event.getTime();
			Long next_pid = (Long) event.get("next_pid");
			if(next_pid > 0) {
				cpuStats.addInterval(eventTs, end, cpu, KernelMode.USER);
			}
		}
	}
	
	public CpuUsageStats getCpuUsageStats() {
		return cpuStats;
	}
}

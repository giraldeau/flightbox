package org.lttng.flightbox.cpu;

import java.util.Set;
import java.util.TreeMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.GlobalState.KernelMode;
import org.lttng.flightbox.UsageStats;
import org.lttng.flightbox.io.EventData;
import org.lttng.flightbox.io.EventQuery;
import org.lttng.flightbox.io.ITraceEventHandler;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;

public class TraceEventHandlerProcess extends TraceEventHandlerBase {
	
	int count; 
	JniTrace trace;

	TreeMap<Long, EventData> cpuHistory;
	TreeMap<Long, EventData> eventHistory;
	UsageStats<Long> procStats;
	TreeMap<Long, KernelProcess> procInfo;
	TreeMap<Long, Long> currentCpuProcess; // (cpu, pid) 
	private int numCpu;
	private double start;
	private double end;
	private TraceReader traceReader;
	
	public TraceEventHandlerProcess() {
		super();
		hooks.add(new TraceHook("kernel", "sched_schedule"));
		hooks.add(new TraceHook("kernel", "process_fork"));
		hooks.add(new TraceHook("fs", "exec"));
		hooks.add(new TraceHook("task_state", "process_state"));
	}
	
	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {
		this.trace = trace;
		count = 0;
		cpuHistory = new TreeMap<Long, EventData>();
		eventHistory = new TreeMap<Long, EventData>();
		numCpu = trace.getCpuNumber();
		start = (double) trace.getStartTime().getTime();
		end = (double) trace.getEndTime().getTime();
		procStats = new UsageStats<Long>((long)start, (long)end, 50);
		procInfo = new TreeMap<Long, KernelProcess>();
		currentCpuProcess = new TreeMap<Long, Long>();
	}
	
	public void handle_kernel_sched_schedule(TraceReader reader, JniEvent event) {
		String eventName = event.getMarkersMap().get(event.getEventMarkerId()).getName();
		
		count++;
		Long cpu = event.getParentTracefile().getCpuNumber();
		double eventTs = (double) event.getEventTime().getTime();
		Long prev_pid = (Long) event.parseFieldByName("prev_pid");
		Long next_pid = (Long) event.parseFieldByName("next_pid");

		// FIXME: how to get the cmd of the process?
		double t = 0;
		if (eventHistory.containsKey(prev_pid)) { // we have a previous event
			t = eventHistory.get(prev_pid).getTime();
		} else { // first event for this CPU
			t = start; 
			eventHistory.put(prev_pid, new EventData());
		}
		
		if (!eventHistory.containsKey(next_pid)) {
			eventHistory.put(next_pid, new EventData());
		}

		if (!cpuHistory.containsKey(cpu)) {
			cpuHistory.put(cpu, new EventData());
		}

		currentCpuProcess.put(cpu, next_pid);
		
		procStats.addInterval(t, eventTs, prev_pid, KernelMode.USER);
		// update history to keep track of previous event
		eventHistory.get(prev_pid).update(event);
		eventHistory.get(next_pid).update(event);
		cpuHistory.get(cpu).update(event);
	}
	
	public void handle_task_state_process_state(TraceReader reader, JniEvent event) {
			KernelProcess proc = new KernelProcess();
			double eventTs = (double) event.getEventTime().getTime();
			Long pid = (Long) event.parseFieldByName("pid");
			proc.setPid(pid);
			proc.setCmd((String) event.parseFieldByName("name"));
			procInfo.put(pid, proc);
			/* FIXME: adding empty interval should not be required */
			procStats.addInterval(eventTs, eventTs, pid, KernelMode.USER);
	}
	
	public void handle_fs_exec(TraceReader reader, JniEvent event) {
			Long cpu = event.getParentTracefile().getCpuNumber();
			Long pid = (Long) cpuHistory.get(cpu).get("next_pid");
			String filename = (String) event.parseFieldByName("filename");
			KernelProcess proc = new KernelProcess();
			proc.setPid(pid);
			proc.setCmd(filename);
			procInfo.put(pid, proc);
	}
	
	public void handle_kernel_process_fork(TraceReader reader, JniEvent event) {
		Long parent_pid = (Long) event.parseFieldByName("parent_pid");
		Long child_pid = (Long) event.parseFieldByName("child_pid");
		KernelProcess proc = new KernelProcess();
		proc.setPid(child_pid);
		proc.setCmd(procInfo.get(parent_pid).getCmd());
		procInfo.put(child_pid, proc);
	}

	@Override
	public void handleComplete(TraceReader reader) {
		// finish intervals until trace end
		EventData event;
		for(Long cpu: cpuHistory.keySet()) {
			// last event for this CPU
			event = cpuHistory.get(cpu);
			long eventTs = event.getTime();
			Long next_pid = (Long) event.get("next_pid");
			procStats.addInterval(eventTs, end, next_pid, KernelMode.USER);
		}
	}
	
	public UsageStats<Long> getUsageStats() {
		return procStats;
	}
	public TreeMap<Long, KernelProcess> getProcInfo() {
		return procInfo;
	}
	
	public KernelProcess getCurrentProcess(Long cpu) {
		Long pid = currentCpuProcess.get(cpu);
		return procInfo.get(pid);
	}

}

package org.lttng.flightbox;

import java.util.HashMap;

import org.lttng.flightbox.GlobalState.KernelMode;

public class CpuUsageStats {

	HashMap<Long, TimeStats> cpuStats;
	
	public CpuUsageStats() {
		cpuStats = new HashMap<Long, TimeStats>();
	}
	
	public HashMap<Long, TimeStats> getStats() {
		return cpuStats;
	}
	
	public void addInterval(Long ts1, Long ts2, Long id, KernelMode mode){
		
	}
}

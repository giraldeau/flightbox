package org.lttng.flightbox.junit;

import org.junit.Test;
import org.lttng.flightbox.CpuUsageStats;
import org.lttng.flightbox.GlobalState.KernelMode;

public class TestCpuUsageStats {

	@Test
	public void testAddInterval() {
		CpuUsageStats stats = new CpuUsageStats();
		// id can be pid or cpuid? same algorithms and datas
		Long ts1 = 10L;
		Long ts2 = 20L; 
		Long id = 0L;
		KernelMode mode = KernelMode.USER;
		stats.addInterval(ts1, ts2, id, mode);
	}
}

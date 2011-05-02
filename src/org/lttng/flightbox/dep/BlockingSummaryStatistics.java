package org.lttng.flightbox.dep;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class BlockingSummaryStatistics {

    SummaryStatistics summary;
    int syscallId;
    
    public BlockingSummaryStatistics(int syscallId) {
        this.syscallId = syscallId;
        summary = new SummaryStatistics();
    }

    public SummaryStatistics getSummary() {
        return summary;
    }

    public int getSyscallId() {
        return this.syscallId;
    }
}

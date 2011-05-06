package org.lttng.flightbox.dep;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class BlockingStatsElement <T> {

    SummaryStatistics summary;
    T id;
    
    public BlockingStatsElement(T id) {
        this.id = id;
        summary = new SummaryStatistics();
    }

    public SummaryStatistics getSummary() {
        return summary;
    }

    public T getId() {
        return this.id;
    }
}

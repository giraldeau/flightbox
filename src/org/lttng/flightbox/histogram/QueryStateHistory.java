package org.lttng.flightbox.histogram;

import statehistory.StateHistorySystem;
import statehistory.common.AttributeNotFoundException;

public class QueryStateHistory {

    public static int[] getSamples(StateHistorySystem shs, int attributeId, int nbSamples) throws AttributeNotFoundException {
        int[] samples = new int[nbSamples];
        int currStateValueInt = 0;
        int prevStateValueInt = 0;
        long tCurr = 0;
        int delta = 0;
        long offset = 0;
        long start = shs.getSHT().getTreeStart();
        long end = shs.getSHT().getTreeEnd();
        long duration = end - start;
        for(int i = 0; i< nbSamples; i++) {
            offset = ((i + 1) * duration) / nbSamples;
            tCurr = offset + start;
            currStateValueInt = shs.getSingleStateValueInt(tCurr, attributeId);
            delta = currStateValueInt - prevStateValueInt;
            samples[i] = delta;
            prevStateValueInt = currStateValueInt;
        }
        return samples;
    }
    
}

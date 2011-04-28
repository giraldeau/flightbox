package org.lttng.flightbox.serial;

import java.util.ArrayList;

public class SampleSeries <X extends Number, Y extends Number> {

    private int nbSamples = 100;

    private SampleIndex<X, Y> index;
    
    private X start;
    private X step;
    private X end;
    
    // step * nbSamples = relative length of the bloc
    
    private ArrayList<Y> samples;
    
    public SampleSeries(SampleIndex<X, Y> index) {
        this.index = index;
    }

    public Y getValue(X x) {
        return samples.get(getArrayIndex(x));
    }
    
    public int getArrayIndex(X x) {
        return (int) (x.doubleValue() * ( end.doubleValue() - start.doubleValue() / nbSamples ));
    }
    
}

package org.lttng.flightbox.serial;

import java.util.ArrayList;
import java.util.HashMap;

public class SampleIndex <X extends Number, Y extends Number> {

    /**
     * key to sample series
     */
    private HashMap<Integer, SampleSeries<X, Y>> seriesIndex;
    
    /**
     * bloc number to offset
     */
    private ArrayList<Integer> blocIndex;

    public SampleIndex() {
        seriesIndex = new HashMap<Integer, SampleSeries<X, Y>>();
    }
    
    public SampleSeries<X, Y> getOrCreate(Integer id) {
        SampleSeries<X, Y> series = seriesIndex.get(id);
        if (series == null) {
            series = new SampleSeries<X, Y>(this);
            seriesIndex.put(id, series);
        }
        return series;
    }
    
    public void put(Integer id, SampleSeries<X, Y> series) {
        seriesIndex.put(id, series);
    }

    private void writeHeader() {
        
    }
    
    private void writeBlocSeries() {
        
    }
    
    private void writeIndex() {
        
    }
    
}

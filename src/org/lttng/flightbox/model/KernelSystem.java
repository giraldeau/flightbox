package org.lttng.flightbox.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class KernelSystem {

	/** 
	 * Current process list
	 */
	private HashMap<Integer, KernelTask> currentTasks;
	
	/**
	 * All process in history
	 */
	private HashSet<KernelTask> allTasks;
	
	/**
	 * All processors
	 */
	private ArrayList<Processor> processors;
	
	/** 
	 * Listeners to register for each processors
	 */
	
	private ArrayList<IProcessorListener> processorListeners;
	
	public KernelSystem() {
		processors = new ArrayList<Processor>();
		processorListeners = new ArrayList<IProcessorListener>();
	}
	
	public void initProcessors(int numOfProcessors) {
		processors.clear();
		for (int i=0; i < numOfProcessors; i++) {
			Processor p = new Processor(i);
			processors.add(p);
			updateProcessorListeners();
		}
	}

	public void updateProcessorListeners() {
		for(IProcessorListener listener: processorListeners) {
			for (Processor processor: processors) {
				processor.addListener(listener);
			}
		}
	}

	public List<Processor> getProcessors() {
		return processors;
	}

	public void registerProcessorListener(IProcessorListener listener) {
		processorListeners.add(listener);
		updateProcessorListeners();
	}
	
}

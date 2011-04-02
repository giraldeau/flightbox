package org.lttng.flightbox.io;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;
import org.lttng.flightbox.model.SystemModel;

public class TraceReader {

	protected String tracePath;
	protected JniTrace trace; 
	private Map<Class, ITraceEventHandler> handlers;
	private Map<String, Map<String, Set<TraceHook>>> traceHookMap;
	private Map<String, Map<Integer, Set<TraceHook>>> traceHookMapCache;
	private Set<TraceHook> catchAllHook;
	private Map<Integer, ArrayList<Set<TraceHook>>> traceHookArrayCache;
	private static Class[] argTypes = new Class[] { TraceReader.class, JniEvent.class };
	private TimeKeeper timeKeeper;
	private SystemModel systemModel;
	
	public TraceReader(String trace_path) {
		this.tracePath = trace_path;
		handlers = new HashMap<Class, ITraceEventHandler>();
		traceHookMap = new HashMap<String, Map<String, Set<TraceHook>>>();
		traceHookMapCache = new HashMap<String, Map<Integer, Set<TraceHook>>>();
		traceHookArrayCache = new HashMap<Integer, ArrayList<Set<TraceHook>>>();
		catchAllHook = new HashSet<TraceHook>();
		timeKeeper = TimeKeeper.getInstance();
	}
	
	public void loadTrace() throws JniException {
		trace = JniTraceFactory.getJniTrace(tracePath);
		systemModel = new SystemModel();
		systemModel.initProcessors(trace.getCpuNumber());
	}

	public void registerHook(ITraceEventHandler handler, TraceHook hook) {
		String methodName;
		if (hook.isAllEvent()) {
			methodName = "handle_all_event";
		} else {
			methodName = "handle_" + hook.channelName + "_" + hook.eventName;
		}
		boolean isHookOk = true;
		Set<TraceHook> eventHooks;
		Map<String, Set<TraceHook>> channelHooks;
		hook.instance = handler;
		try {
			hook.method = handler.getClass().getMethod(methodName, argTypes);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isHookOk = false;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isHookOk = false;
		}
		if (!isHookOk)
			return;
		
		if(hook.isAllEvent()) {
			catchAllHook.add(hook);
		} else {
			channelHooks = traceHookMap.get(hook.channelName);
			if (channelHooks == null) {
				channelHooks = new HashMap<String, Set<TraceHook>>();
				traceHookMap.put(hook.channelName, channelHooks);
			}
			eventHooks = channelHooks.get(hook.eventName);
			if (eventHooks == null) {
				eventHooks = new HashSet<TraceHook>();
				channelHooks.put(hook.eventName, eventHooks);
			}
			eventHooks.add(hook);
		}	
	}
	
	public void register(ITraceEventHandler handler) {
		Set<TraceHook> handlerHooks = handler.getHooks();
		
		/* If handlerHooks is null then add no hooks */
		if (handlerHooks == null || handlerHooks.size() == 0) {
			return;
		}
		
		/* register individual hooks */
		for (TraceHook hook: handlerHooks) {
			registerHook(handler, hook);
		}

		/* FIXME: should we remove the handler if an exception 
		 * occur in registering hooks? */
		handlers.put(handler.getClass(), handler);
	}
	
	public void process() throws JniException {
		loadTrace();
		JniEvent event;
		int nbEvents = 0;
		int eventId;
		Set<TraceHook> hooks;
		String eventName;
		String traceFileName;
		
		for(ITraceEventHandler handler: handlers.values()) {
			handler.handleInit(this, trace);
		}
		
		while((event=trace.readNextEvent()) != null) {
			timeKeeper.setCurrentTime(event.getEventTime().getTime());
			nbEvents++;
			//eventId = event.getEventMarkerId();
			traceFileName = event.getParentTracefile().getTracefileName();
			eventId = event.getEventMarkerId();
			eventName = event.getMarkersMap().get(event.getEventMarkerId()).getName();
			
			hooks = getHookSetByIdArrayHashCode(traceFileName, traceFileName.hashCode(), eventName, eventId);
			
			// FIXME: remove hook if an exception is raised
			for (TraceHook h: hooks){
				try {
					h.method.invoke(h.instance, this, event);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			
			for (TraceHook h: catchAllHook) {
				try {
					h.method.invoke(h.instance, this, event);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		for(ITraceEventHandler handler: handlers.values()) {
			handler.handleComplete(this);
		}
	}
	
	public Set<TraceHook> getHookSetByName(String channelName, String eventName) {
		Map<String, Set<TraceHook>> channelHooks;
		Set<TraceHook> hooks = null;
		channelHooks = traceHookMap.get(channelName);
		if (channelHooks == null) {
			channelHooks = new HashMap<String, Set<TraceHook>>();
			traceHookMap.put(channelName, channelHooks);
		}
		hooks = channelHooks.get(eventName);
		if (hooks == null) {
			hooks = new HashSet<TraceHook>();
			channelHooks.put(eventName, hooks);
		}
		return hooks;
	}

	public Set<TraceHook> getHookSetByIdArrayHashCode(String channelName, Integer channelHC, String eventName, int eventId) {
		ArrayList<Set<TraceHook>> channelHooksCache;
		Set<TraceHook> hooks = null;
		channelHooksCache = traceHookArrayCache.get(channelHC);
		
		if (channelHooksCache == null) {
			channelHooksCache = new ArrayList<Set<TraceHook>>();
			traceHookArrayCache.put(channelHC, channelHooksCache);
		}
		if (channelHooksCache.size() <= eventId) {
			for(int i=channelHooksCache.size(); i<eventId+1; i++) {
				channelHooksCache.add(null);
			}
		}
		hooks = channelHooksCache.get(eventId);
		if (hooks == null) {
			/* we don't have it in the cache, search for it in the traceHookMap */
			hooks = getHookSetByName(channelName, eventName);
			channelHooksCache.set(eventId, hooks);
		}
		return hooks;
	}
	
	public ITraceEventHandler getHandler(
			Class<? extends TraceEventHandlerBase> klass) {
		return handlers.get(klass);
	}
	
	public Long getStartTime() {
		return trace.getStartTime().getTime();
	}
	
	public Long getEndTime() {
		return trace.getEndTime().getTime();
	}
	
	public SystemModel getSystemModel() {
		return systemModel;
	}
}

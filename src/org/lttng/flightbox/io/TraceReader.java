package org.lttng.flightbox.io;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;

public class TraceReader {

	private String tracePath;
	private JniTrace trace; 
	private Map<Class, ITraceEventHandler> handlers;
	private Map<String, Map<String, Set<TraceHook>>> traceHookMap;
	private Set<TraceHook> catchAllHook;
	private Set<TraceHook> emptyHook;
	private static Class[] argTypes = new Class[] { TraceReader.class, JniEvent.class };
	
	public TraceReader(String trace_path) {
		this.tracePath = trace_path;
		handlers = new HashMap<Class, ITraceEventHandler>();
		traceHookMap = new TreeMap<String, Map<String, Set<TraceHook>>>();
		catchAllHook = new HashSet<TraceHook>();
		emptyHook = new HashSet<TraceHook>();
	}
	
	public void loadTrace() throws JniException {
		trace = JniTraceFactory.getJniTrace(tracePath);
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
				channelHooks = new TreeMap<String, Set<TraceHook>>();
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
		Map<String, Set<TraceHook>> channelHooks;
		String eventName;
		String traceFileName;
		
		
		for(ITraceEventHandler handler: handlers.values()) {
			handler.handleInit(this, trace);
		}
		
		while((event=trace.readNextEvent()) != null) {
			nbEvents++;
			//eventId = event.getEventMarkerId();
			traceFileName = event.getParentTracefile().getTracefileName();
			eventName = event.getMarkersMap().get(event.getEventMarkerId()).getName();
			
			/* look in the cache first */
			/* does java intern strings automatically? */
			/* can't on a TreeMap: compareTo is used, hence may involve a lot of comparison */
			
			hooks = getHookSet(traceFileName, eventName);
			
			for (TraceHook h: hooks){
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
	
	public Set<TraceHook> getHookSet(String channelName, String eventName) {
		Map<String, Set<TraceHook>> channelHooks;
		Set<TraceHook> hooks = null;
		channelHooks = traceHookMap.get(channelName);
		if (channelHooks != null) {
			hooks = channelHooks.get(eventName);
		}
		if (hooks == null) {
			hooks = emptyHook;
		}
		return hooks;
	}

	public ITraceEventHandler getHandler(
			Class<? extends TraceEventHandlerBase> class1) {
		return null;
	}
}

package org.lttng.flightbox.io;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;
import org.lttng.flightbox.model.SystemModel;

public class TraceReader {

	protected String tracePath;
	protected JniTrace trace;
	private final Map<Class, ITraceEventHandler> handlers;
	private final Map<String, Map<String, TreeSet<TraceHook>>> traceHookMap;
	private final Map<String, Map<Integer, TreeSet<TraceHook>>> traceHookMapCache;
	private final TreeSet<TraceHook> catchAllHook;
	private final Map<Integer, ArrayList<TreeSet<TraceHook>>> traceHookArrayCache;
	private static Class[] argTypes = new Class[] { TraceReader.class, JniEvent.class };
	private final TimeKeeper timeKeeper;
	private SystemModel systemModel;
	private boolean cancel;

	public TraceReader(String trace_path) {
		this.tracePath = trace_path;
		handlers = new HashMap<Class, ITraceEventHandler>();
		traceHookMap = new HashMap<String, Map<String, TreeSet<TraceHook>>>();
		traceHookMapCache = new HashMap<String, Map<Integer, TreeSet<TraceHook>>>();
		traceHookArrayCache = new HashMap<Integer, ArrayList<TreeSet<TraceHook>>>();
		catchAllHook = new TreeSet<TraceHook>();
		timeKeeper = TimeKeeper.getInstance();
	}

	public void loadTrace() throws JniException {
	    if (trace != null) {
	        trace.closeTrace();
	    }
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
		TreeSet<TraceHook> eventHooks;
		Map<String, TreeSet<TraceHook>> channelHooks;
		hook.instance = handler;
		try {
			hook.method = handler.getClass().getMethod(methodName, argTypes);
		} catch (SecurityException e) {
			e.printStackTrace();
			isHookOk = false;
		} catch (NoSuchMethodException e) {
			System.err.println("Error: hook " + handler.getClass() + "." + methodName + " doesn't exist, disabling");
			isHookOk = false;
		}
		if (!isHookOk)
			return;

		if(hook.isAllEvent()) {
			catchAllHook.add(hook);
		} else {
			channelHooks = traceHookMap.get(hook.channelName);
			if (channelHooks == null) {
				channelHooks = new HashMap<String, TreeSet<TraceHook>>();
				traceHookMap.put(hook.channelName, channelHooks);
			}
			eventHooks = channelHooks.get(hook.eventName);
			if (eventHooks == null) {
				eventHooks = new TreeSet<TraceHook>();
				channelHooks.put(hook.eventName, eventHooks);
			}
			eventHooks.add(hook);
		}
	}

	public void register(ITraceEventHandler handler) {
		if (handler == null)
			return;
		Set<TraceHook> handlerHooks = handler.getHooks();

		/* If handlerHooks is null then add no hooks */
		if (handlerHooks == null || handlerHooks.size() == 0) {
			return;
		}

		/* register individual hooks */
		for (TraceHook hook: handlerHooks) {
			registerHook(handler, hook);
		}

		handlers.put(handler.getClass(), handler);
	}

	public void process() throws JniException {
		loadTrace();
		JniEvent event;
		int nbEvents = 0;
		int eventId;
		TreeSet<TraceHook> hooks;
		String eventName;
		String traceFileName;
		cancel = false;

		for(ITraceEventHandler handler: handlers.values()) {
			handler.handleInit(this, trace);
		}

		while((event=trace.readNextEvent()) != null && cancel != true) {
			timeKeeper.setCurrentTime(event.getEventTime().getTime());
			nbEvents++;
			//eventId = event.getEventMarkerId();
			traceFileName = event.getParentTracefile().getTracefileName();
			eventId = event.getEventMarkerId();
			eventName = event.getMarkersMap().get(event.getEventMarkerId()).getName();

			hooks = getHookSetByIdArrayHashCode(traceFileName, traceFileName.hashCode(), eventName, eventId);
			runHookSet(hooks, event);
			runHookSet(catchAllHook, event);

		}

		for(ITraceEventHandler handler: handlers.values()) {
			handler.handleComplete(this);
		}

		trace.closeTrace();
	}

	public void runHookSet(TreeSet<TraceHook> hooks, JniEvent event) {
		for (TraceHook h: hooks){
			try {
				h.method.invoke(h.instance, this, event);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				cancel = true;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				cancel = true;
			} catch (InvocationTargetException e) {
				System.err.println("error while executing " + h.method + " on " + h.instance);
				e.printStackTrace();
				cancel = true;
			}
		}		
	}
	
	public TreeSet<TraceHook> getHookSetByName(String channelName, String eventName) {
		Map<String, TreeSet<TraceHook>> channelHooks;
		TreeSet<TraceHook> hooks = null;
		channelHooks = traceHookMap.get(channelName);
		if (channelHooks == null) {
			channelHooks = new HashMap<String, TreeSet<TraceHook>>();
			traceHookMap.put(channelName, channelHooks);
		}
		hooks = channelHooks.get(eventName);
		if (hooks == null) {
			hooks = new TreeSet<TraceHook>();
			channelHooks.put(eventName, hooks);
		}
		return hooks;
	}

	public TreeSet<TraceHook> getHookSetByIdArrayHashCode(String channelName, Integer channelHC, String eventName, int eventId) {
		ArrayList<TreeSet<TraceHook>> channelHooksCache;
		TreeSet<TraceHook> hooks = null;
		channelHooksCache = traceHookArrayCache.get(channelHC);

		if (channelHooksCache == null) {
			channelHooksCache = new ArrayList<TreeSet<TraceHook>>();
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

	public void cancel() {
		this.cancel = true;
	}
	public Boolean isCancel() {
		return this.cancel;
	}
}

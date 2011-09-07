package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.TreeSet;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.ITaskListener;
import org.lttng.flightbox.model.Processor;
import org.lttng.flightbox.model.ProcessorListener;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.SyscallInfo;

public class TestSystemModel {

	class MyListener extends ProcessorListener {
		public boolean mode = false;
		@Override
		public void lowPowerModeChange(Processor processor, boolean nextlowPowerMode) {
			mode = nextlowPowerMode;
		}
	}

	@Test
	public void testSystemListeners() {
		MyListener listener = new MyListener();
		SystemModel model = new SystemModel();
		model.initProcessors(2);
		model.addProcessorListener(listener);
		model.getProcessors().get(1).setLowPowerMode(true);
		assertEquals(true, listener.mode);
	}
	
	class TaskListenerForkExit implements ITaskListener {

		private SystemModel model;

		@Override
		public void pushState(Task task, StateInfo nextState) {
			if (task.getCmd().compareTo("/usr/local/bin/inception") == 0) {
				dumpInfo(task, task.peekState(), nextState, "push");
			}
		}

		@Override
		public void popState(Task task, StateInfo nextState) {
			if (task.getCmd().compareTo("/usr/local/bin/inception") == 0) {
				dumpInfo(task, task.peekState(), nextState, "pop");
			}
		}

		public void setModel(SystemModel model) {
			this.model = model;
		}
		
		public void dumpInfo(Task task, StateInfo currState, StateInfo nextState, String type) {
			StringBuilder str = new StringBuilder();
			str.append(String.format("%d %5s ", task.getProcessId(), type));
			str.append(String.format("%10s ", currState != null ? currState.getTaskState() : ""));
			str.append(String.format("%10s ", nextState != null ? nextState.getTaskState() : ""));
			if (currState != null && currState instanceof SyscallInfo) {
				SyscallInfo state = (SyscallInfo) currState;
				str.append(String.format("%20s", model.getSyscallTable().get(state.getSyscallId())));
			}
			if (nextState != null && nextState instanceof SyscallInfo) {
				SyscallInfo state = (SyscallInfo) nextState;
				str.append(String.format("%20s", model.getSyscallTable().get(state.getSyscallId())));
			}
			System.out.println(str.toString());
		}
		
	}
	
	@Test
	public void testTaskListenerForkExit() throws JniException {
		String trace = "inception-3x-100ms";
		
		File file = new File(Path.getTraceDir(), trace);
		// make sure we have this trace
		assertTrue("Missing trace " + trace, file.isDirectory());

		String tracePath = file.getPath();
		SystemModel model = new SystemModel();
		TaskListenerForkExit taskListener = new TaskListenerForkExit();
		taskListener.setModel(model);
		model.addTaskListener(taskListener);
		ModelBuilder.buildFromTrace(tracePath, model);

		TreeSet<Task> taskSet = model.getTaskByCmdBasename("inception");
		
	} 
}

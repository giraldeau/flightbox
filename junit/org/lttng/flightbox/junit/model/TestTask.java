package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;
import org.lttng.flightbox.model.IRQInfo;
import org.lttng.flightbox.model.StateInfo;
import org.lttng.flightbox.model.SyscallInfo;
import org.lttng.flightbox.model.SyscallInfo.Field;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.TaskListener;

public class TestTask {

	@Test
	public void testCompareTasks() {
		Task t1 = new Task(1, 10L);
		Task t2 = new Task(1, 10L);
		Task t3 = new Task(2, 10L);
		Task t4 = new Task(1, 20L);

		assertEquals(0, t1.compareTo(t2));
		assertEquals(-1, t1.compareTo(t3));
		assertEquals(-1, t1.compareTo(t4));
		assertEquals(-1, t4.compareTo(t3));
		assertEquals(1, t3.compareTo(t4));
	}

	class MyTaskListener extends TaskListener {
		public HashMap<Task, TaskState> state = new HashMap<Task, TaskState>();
		@Override
		public void pushState(Task task, TaskState nextState) {
			state.put(task, nextState);
		}
		@Override
		public void popState(Task task, TaskState nextState) {
			state.put(task, nextState);
		}
	}

	@Test
	public void testTaskListener() {
		Task t1 = new Task(1, 10L);
		SystemModel model = new SystemModel();
		MyTaskListener listener = new MyTaskListener();

		model.addTask(t1);
		model.addTaskListener(listener);

		assertFalse(listener.state.containsKey(t1));
		t1.pushState(TaskState.USER);
		assertTrue(listener.state.containsKey(t1));
		assertEquals(TaskState.USER, listener.state.get(t1));

		t1.pushState(TaskState.SYSCALL);
		assertEquals(TaskState.SYSCALL, listener.state.get(t1));

		t1.popState();
		assertEquals(TaskState.USER, listener.state.get(t1));

		t1.popState();
		assertEquals(null, listener.state.get(t1));

	}

	class StateInfoTaskListener extends TaskListener {
		public HashMap<Task, TaskState> state = new HashMap<Task, TaskState>();
		public StateInfo currStateInfo;
		public StateInfo nextStateInfo;
		@Override
		public void pushState(Task task, TaskState nextState) {
			state.put(task, nextState);
			nextStateInfo = task.peekStateInfo(nextState);
			currStateInfo = task.peekStateInfo(task.peekState());
		}
	}

	@Test
	public void testTaskStateInfo() {
		Task t1 = new Task(1, 10L);
		StateInfoTaskListener listener = new StateInfoTaskListener();
		t1.addListener(listener);

		// syscall_entry simulation for syscall_open with nested IRQ
		assertNull(t1.peekState());
		SyscallInfo syscallInfo = (SyscallInfo) t1.setupStateInfo(TaskState.SYSCALL);
		syscallInfo.setSyscallId(42);
		t1.pushState(TaskState.SYSCALL);
		assertEquals(42, ((SyscallInfo)listener.nextStateInfo).getSyscallId());

		// fs.open: populate syscall info
		SyscallInfo info1 = (SyscallInfo) t1.peekStateInfo(t1.peekState());
		info1.setField(Field.FILENAME, "test-file");

		// irq_entry
		IRQInfo info2 = (IRQInfo) t1.setupStateInfo(TaskState.IRQ);
		info2.setIRQId(123);
		t1.pushState(TaskState.IRQ);

		// irq_exit
		t1.popState();

		// syscall_exit
		t1.popState();
		assertEquals("test-file", ((SyscallInfo)listener.currStateInfo).getField(Field.FILENAME));
	}
}

package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;
import org.lttng.flightbox.io.TimeKeeper;
import org.lttng.flightbox.model.IRQInfo;
import org.lttng.flightbox.model.StateInfo;
import org.lttng.flightbox.model.StateInfoFactory;
import org.lttng.flightbox.model.SyscallInfo;
import org.lttng.flightbox.model.SyscallInfo.Field;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.TaskListener;
import org.lttng.flightbox.model.WaitInfo;
import org.lttng.flightbox.model.WaitInfo.WaitType;

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
		public HashMap<Task, StateInfo> state = new HashMap<Task, StateInfo>();
		@Override
		public void pushState(Task task, StateInfo nextState) {
			state.put(task, nextState);
		}
		@Override
		public void popState(Task task, StateInfo nextState) {
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
		t1.pushState(StateInfoFactory.makeStateInfo(TaskState.USER));
		assertTrue(listener.state.containsKey(t1));
		assertEquals(TaskState.USER, listener.state.get(t1).getTaskState());

		t1.pushState(StateInfoFactory.makeStateInfo(TaskState.SYSCALL));
		assertEquals(TaskState.SYSCALL, listener.state.get(t1).getTaskState());

		t1.popState();
		assertEquals(TaskState.USER, listener.state.get(t1).getTaskState());

		t1.popState();
		assertEquals(null, listener.state.get(t1));

	}

	class StateInfoTaskListener extends TaskListener {
		public HashMap<Task, StateInfo> state = new HashMap<Task, StateInfo>();
		public StateInfo currStateInfo;
		public StateInfo nextStateInfo;
		@Override
		public void pushState(Task task, StateInfo nextState) {
			state.put(task, nextState);
			nextStateInfo = nextState;
			currStateInfo = task.peekState();
		}
	}

	@Test
	public void testTaskStateInfo() {
		Task t1 = new Task(1, 10L);
		StateInfoTaskListener listener = new StateInfoTaskListener();
		t1.addListener(listener);

		// syscall_entry simulation for syscall_open with nested IRQ
		assertNull(t1.peekState());
		SyscallInfo syscallInfo = (SyscallInfo) StateInfoFactory.makeStateInfo(TaskState.SYSCALL);
		syscallInfo.setSyscallId(42);
		t1.pushState(syscallInfo);
		assertEquals(42, ((SyscallInfo)listener.nextStateInfo).getSyscallId());

		// fs.open: populate syscall info
		SyscallInfo info1 = (SyscallInfo) t1.peekState();
		info1.setField(Field.FILENAME, "test-file");

		// irq_entry
		IRQInfo info2 = (IRQInfo) StateInfoFactory.makeStateInfo(TaskState.IRQ);
		info2.setIRQId(123);
		t1.pushState(info2);

		// irq_exit
		t1.popState();

		// syscall_exit
		t1.popState();
		assertEquals("test-file", ((SyscallInfo)listener.currStateInfo).getField(Field.FILENAME));
	}

	class BlockingListener extends TaskListener {
		public HashMap<Task, WaitInfo> blockingInfo = new HashMap<Task, WaitInfo>();
		@Override
		public void popState(Task task, StateInfo nextState) {
			StateInfo state = task.peekState();
			if (state == null)
				return;
			if (state instanceof WaitInfo) {
				WaitInfo info = (WaitInfo) state;
				if (info.isBlocking())
					blockingInfo.put(task, info);
			}
		}
	}

	@Test
	public void testTaskDetectBlocking() {
		Task t1 = new Task(1, 10);
		Task t2 = new Task(1, 10);
		TimeKeeper time = TimeKeeper.getInstance();
		time.setCurrentTime(10);

		BlockingListener listener = new BlockingListener();
		t1.addListener(listener);

		// task initial state
		t1.pushState(StateInfoFactory.makeStateInfo(TaskState.USER));
		t2.pushState(StateInfoFactory.makeStateInfo(TaskState.USER));
		t2.pushState(StateInfoFactory.makeStateInfo(TaskState.WAIT));

		// syscall_entry simulation for syscall_nanosleep
		time.setCurrentTime(20);
		SyscallInfo info1 = (SyscallInfo) StateInfoFactory.makeStateInfo(TaskState.SYSCALL);
		info1.setSyscallId(162);
		t1.pushState(info1);

		/* scheduled out of the task, we don't know the wait type yet
		 * it can be either an expired time slice or blocking on a resource
		 * try wakeup event determine the wait type
		 */
		time.setCurrentTime(30);
		WaitInfo info2 = (WaitInfo) StateInfoFactory.makeStateInfo(TaskState.WAIT);
		info2.setWaitParent(t1.peekState());
		t1.pushState(info2);
		t2.popState();

		// irq_entry timer on t2
		time.setCurrentTime(40);
		IRQInfo info3 = (IRQInfo) StateInfoFactory.makeStateInfo(TaskState.IRQ);
		info3.setIRQId(0);
		t2.pushState(info3);
		// try_wakeup on t1
		time.setCurrentTime(45);
		WaitInfo stateInfo1 = (WaitInfo) t1.peekState();
		IRQInfo stateInfo2 = (IRQInfo) t2.peekState();
		// if the task is waiting, then we know what wake it up
		stateInfo1.setWakeUp(stateInfo2);
		stateInfo1.setWait(WaitType.TIMER);
		stateInfo1.setBlocking(true);

		t1.popState();
		// but still, the task is not scheduled yet
		WaitInfo info6 = (WaitInfo) StateInfoFactory.makeStateInfo(TaskState.WAIT);
		t1.pushState(info6);

		// irq_exit timer
		time.setCurrentTime(50);
		t2.popState();

		/* scheduled in of the blocked task */
		time.setCurrentTime(60);
		WaitInfo info7 = (WaitInfo) StateInfoFactory.makeStateInfo(TaskState.WAIT);
		t2.pushState(info7);
		t1.popState();

		// syscall_exit
		t1.popState();

		WaitInfo wait = listener.blockingInfo.get(t1);
		assertEquals(WaitType.TIMER, wait.getWait());
		assertEquals(0, ((IRQInfo)wait.getWakeUp()).getIRQId());
		assertEquals(true, wait.isBlocking());
		assertEquals(162, ((SyscallInfo)wait.getWaitParent()).getSyscallId());
	}
}

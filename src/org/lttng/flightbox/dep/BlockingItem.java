package org.lttng.flightbox.dep;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.lttng.flightbox.model.SocketInet;
import org.lttng.flightbox.model.SymbolTable;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.SoftIRQInfo;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.SyscallInfo;
import org.lttng.flightbox.model.state.StateInfo.Field;
import org.lttng.flightbox.statistics.ResourceUsage;

public class BlockingItem implements Comparable<BlockingItem> {

	private Task task;
	private StateInfo wakeUp;
	private Task wakeUpTask;
	private SyscallInfo waitingSyscall;
	private long startTime;
	private long endTime;
	private TreeSet<BlockingItem> children;
	private CpuAccountingItem cpuAccounting;
	
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public StateInfo getWakeUp() {
		return wakeUp;
	}
	public void setWakeUp(StateInfo wakeUp) {
		this.wakeUp = wakeUp;
	}
	public SyscallInfo getWaitingSyscall() {
		return waitingSyscall;
	}
	public void setWaitingSyscall(SyscallInfo waitingSyscall) {
		this.waitingSyscall = waitingSyscall;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public long getDuration() {
	    return this.endTime - this.startTime;
	}
	
	public TreeSet<BlockingItem> getChildren(SystemModel model) {
		if (children != null) {
			return children;
		}
		children = new TreeSet<BlockingItem>();
		if (wakeUp == null)
			return children;
		
		BlockingModel bm = model.getBlockingModel();
		Task subTask = getSubTask(model);
		populateSubBlocking(bm, children, subTask);
		return children;
	}

	public Task getSubTask(SystemModel model) {
		if (wakeUp.getTaskState() == TaskState.EXIT) {
			return wakeUp.getTask();
		} else if (wakeUp.getTaskState() == TaskState.SOFTIRQ) {
			/* 
			 * wakeup from a received packet : wakeup always in softirq
			 * socket info either in softirq or syscall
			 * get the task associated with this socket
			 */
			SocketInet srvSocket = new SocketInet();
			if (wakeUp.getField(Field.SRC_ADDR) != null) {
				copySocketInfo(wakeUp, srvSocket);
			} else if (waitingSyscall.getField(Field.SRC_ADDR) != null) {
				copySocketInfo(waitingSyscall, srvSocket);
			}
			if (srvSocket.isSet()) {
				HashMap<Integer, TreeSet<Task>> tasks = model.getTasks();
				for (Integer pid: tasks.keySet()) {
					Task task = tasks.get(pid).last();
					if (task.matchSocket(srvSocket)) {
						return task;
					}
				}
			}
		}
		return null;
	}
	
	private void copySocketInfo(StateInfo info, SocketInet sock) {
		if ((Boolean)info.getField(Field.IS_XMIT)) {
			sock.setSrcAddr((Long) info.getField(Field.SRC_ADDR));
			sock.setDstAddr((Long) info.getField(Field.DST_ADDR));
			sock.setSrcPort((Integer) info.getField(Field.SRC_PORT));
			sock.setDstPort((Integer) info.getField(Field.DST_PORT));
		} else {
			sock.setSrcAddr((Long) info.getField(Field.DST_ADDR));
			sock.setDstAddr((Long) info.getField(Field.SRC_ADDR));
			sock.setSrcPort((Integer) info.getField(Field.DST_PORT));
			sock.setDstPort((Integer) info.getField(Field.SRC_PORT));			
		}
	}
	
	public void populateSubBlocking(BlockingModel blockingModel, TreeSet<BlockingItem> subBlock, Task subTask) {
		if (startTime >= endTime) {
			System.out.println("prob: startTime=" + startTime + " endTime=" + endTime + " diff=" + (endTime - startTime));
		}
		TreeSet<BlockingItem> items = blockingModel.getBlockingItemsForTask(subTask);
		BlockingItem fromElement = new BlockingItem();
		fromElement.setStartTime(startTime);
		BlockingItem toElement = new BlockingItem();
		toElement.setStartTime(endTime);
		SortedSet<BlockingItem> subSet = items.subSet(fromElement, toElement);
		subBlock.addAll(subSet);
	}
	
	@Override
	public int compareTo(BlockingItem o) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		if (o == this) return EQUAL;
		if (this.startTime < o.startTime) return BEFORE;
		if (this.startTime > o.startTime) return AFTER;
		return EQUAL;
	}
	public void setWakeUpTask(Task wakeUpTask) {
		this.wakeUpTask = wakeUpTask;
	}
	public Task getWakeUpTask() {
		return wakeUpTask;
	}
	
}

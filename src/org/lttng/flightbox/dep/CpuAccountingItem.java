package org.lttng.flightbox.dep;

import java.util.SortedSet;
import java.util.TreeSet;

import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.statistics.Bucket;
import org.lttng.flightbox.statistics.BucketSeries;
import org.lttng.flightbox.statistics.ResourceUsage;

public class CpuAccountingItem implements Comparable<CpuAccountingItem> {

	private Task task;
	private BlockingItem item;
	private double selfTime = 0.0;
	private double subTime = 0.0;
	private boolean updateSelfTime = true;
	private boolean updateSubTime = true;
	private boolean updateChildren = true;
	private TreeSet<CpuAccountingItem> children;
	
	
	public CpuAccountingItem(Task task) {
		this.task = task;
	}

	public TreeSet<CpuAccountingItem> getChildren(SystemModel model, ResourceUsage<Long> cpuStats) {
		if (updateChildren) {
			children = new TreeSet<CpuAccountingItem>();
			/* there is one subtask max per blocking item */
			BlockingModel bm = model.getBlockingModel();
			TreeSet<BlockingItem> allItems = bm.getBlockingItemsForTask(task);
			TreeSet<BlockingItem> subItems = new TreeSet<BlockingItem>();
			if (item != null) {
				BlockingItem fromElement = new BlockingItem();
				BlockingItem toElement = new BlockingItem();
				fromElement.setStartTime(item.getStartTime());
				toElement.setStartTime(item.getEndTime());
				SortedSet<BlockingItem> subSet = allItems.subSet(fromElement, toElement);
				subItems.addAll(subSet);
			} else {
				subItems = allItems;
			}
			for (BlockingItem subItem: subItems) {
				Task subTask = subItem.getSubTask(model);
				if (subTask != null) {
					CpuAccountingItem acc = new CpuAccountingItem(subTask);
					acc.setItem(subItem);
					children.add(acc);
				}
			}
			updateChildren = false;
		}
		return children;
	}
	
	/* 
	 * don't use time window of bloking item
	 * will always yield 0! the task is never scheduled when blocked
	 */
	public double getSelfTime(SystemModel model, ResourceUsage<Long> cpuStats) {
		if (updateSelfTime) {
			long t1 = 0, t2 = 0;
			if (item == null) {
				t1 = task.getStartTime();
				t2 = task.getEndTime();
			} else {
				t1 = item.getStartTime();
				t2 = item.getEndTime();
			}
			BucketSeries stats = cpuStats.getStats((long)task.getProcessId());
			Bucket sum = stats.getSum(t1, t2);
			selfTime = sum.getTime(TaskState.USER);
			updateSelfTime = false;
		}
		return selfTime;
	}
	
	public double getSubtaskTime(SystemModel model, ResourceUsage<Long> cpuStats) {
		if (updateSubTime) {
			/* there is one subtask max per blocking item */
			BlockingModel bm = model.getBlockingModel();
			TreeSet<BlockingItem> tmpSubItems = bm.getBlockingItemsForTask(task);
			for (BlockingItem item: tmpSubItems) {
				long t1 = item.getStartTime();
				long t2 = item.getEndTime();
				Task subTask = item.getSubTask(model);
				if (subTask != null) {
					BucketSeries stats = cpuStats.getStats((long)subTask.getProcessId());
					Bucket sum = stats.getSum(t1, t2);
					subTime += sum.getTime(TaskState.USER);
				}
			}
			updateSubTime = false;
		}
		return subTime;		
	}

	public void setItem(BlockingItem parentItem) {
		this.item = parentItem;
		this.updateSelfTime = true;
		this.updateSubTime = true;
		this.updateChildren = true;
	}

	public BlockingItem getItem() {
		return item;
	}

	@Override
	public int compareTo(CpuAccountingItem o) {
		BlockingItem other = o.getItem();
		if (other == this.item || other == null)
			return 0;
		return item.compareTo(other);
	}

	public Task getTask() {
		return this.task;
	}
	
}
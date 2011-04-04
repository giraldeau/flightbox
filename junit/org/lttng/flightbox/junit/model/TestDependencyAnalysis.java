package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.dep.BlockingItem;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.WaitInfo;

public class TestDependencyAnalysis {

	@Test
	public void testNanosleep() throws JniException {
		String tracePath = new File(Path.getTraceDir(), "sleep-1x-1sec").getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		// verify there is /bin/sleep task as child of sleep-1x-1sec
		Task foundTask = model.getLatestTaskByCmdBasename("sleep");
		List<BlockingItem> blockingItems = listener.getBlockingItems();
		List<BlockingItem> taskItems = new ArrayList<BlockingItem>();
		for (BlockingItem item: blockingItems) {
			if (item.getTask() == foundTask) {
				taskItems.add(item);
			}
		}

		assertEquals(1, taskItems.size());
		WaitInfo info = taskItems.get(0).getWaitInfo();
		double duration = info.getEndTime() - info.getStartTime();
		assertEquals(1000000000.0, duration, 10000000.0);
	}

}

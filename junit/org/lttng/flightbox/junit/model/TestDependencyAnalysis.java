package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
import org.lttng.flightbox.dep.BlockingModel;
import org.lttng.flightbox.dep.BlockingStats;
import org.lttng.flightbox.dep.BlockingStatsElement;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.dep.BlockingItem;
import org.lttng.flightbox.dep.CpuAccountingItem;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.DiskFile;
import org.lttng.flightbox.model.FileDescriptor;
import org.lttng.flightbox.model.SocketInet;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.statistics.ResourceUsage;

public class TestDependencyAnalysis {

	//@Test
	public void testNanosleep() throws JniException {
		String tracePath = new File(Path.getTraceDir(), "sleep-1x-1sec").getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		BlockingModel bm = model.getBlockingModel();
		
		Task foundTask = model.getLatestTaskByCmdBasename("sleep");
		SortedSet<BlockingItem> taskItems = bm.getBlockingItemsForTask(foundTask);

		assertTrue(taskItems.size() >= 1);
		BlockingItem info = taskItems.last();
		double duration = info.getEndTime() - info.getStartTime();
		assertEquals(1000000000.0, duration, 10000000.0);
	}

	//@Test
	public void testInception() throws JniException {
		String trace = "inception-3x-100ms";
		File file = new File(Path.getTraceDir(), trace);
		// make sure we have this trace
		assertTrue("Missing trace " + trace, file.isDirectory());

		String tracePath = file.getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		BlockingModel bm = model.getBlockingModel();
		
		// get the last spawned child
		Task foundTask = model.getLatestTaskByCmdBasename("inception");
		SortedSet<BlockingItem> taskItems = bm.getBlockingItemsForTask(foundTask);

		// 100ms + 200ms + 400ms = 700ms
		assertTrue(taskItems.size() >= 1);
		BlockingItem info = taskItems.last();
		double duration = info.getEndTime() - info.getStartTime();
		assertEquals(400000000.0, duration, 10000000.0);

		// verify recovered blocking information
		Task master = foundTask.getParentProcess().getParentProcess();
		SortedSet<BlockingItem> masterItems = bm.getBlockingItemsForTask(master);
		BlockingItem nanoSleep = null, waitPid = null;
		int SYS_NANOSLEEP = 162;
		int SYS_WAITPID = 7;
		for (BlockingItem item: masterItems) {
			assertNotNull(item.getWakeUp());
			if (item.getWaitingSyscall().getSyscallId() == SYS_NANOSLEEP) {
			    nanoSleep = item;
			} else if (item.getWaitingSyscall().getSyscallId() == SYS_WAITPID) {
			    waitPid = item;
			}
		}
		
		assertNotNull(nanoSleep);
		assertNotNull(waitPid);
		double p = 10000000;
		assertEquals(nanoSleep.getDuration(), 100000000, p);
		assertEquals(waitPid.getDuration(), 600000000, p);
	}

	//@Test
	public void testRcpHog() throws JniException {
		String trace = "rpc-sleep-100ms";
		File file = new File(Path.getTraceDir(), trace);
		// make sure we have this trace
		assertTrue("Missing trace " + trace, file.isDirectory());

		String tracePath = file.getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		BlockingModel bm = model.getBlockingModel();
		Task foundTask = model.getLatestTaskByCmdBasename("clihog");
		SortedSet<BlockingItem> taskItems = bm.getBlockingItemsForTask(foundTask);
		assertTrue(taskItems.size() >= 1);
		
		Task server = model.getLatestTaskByCmdBasename("srvhog");
		HashMap<Integer, TreeSet<FileDescriptor>> serverFds = server.getFileDescriptors();
		SocketInet srvSock = (SocketInet) serverFds.get(4).last();
		assertEquals(9876, srvSock.getSrcPort());
		
		BlockingItem read = taskItems.last();
		double p = 10000000;
		assertEquals(read.getDuration(), 100000000, p);
		
		TreeSet<BlockingItem> children = read.getChildren(model);
		assertTrue(children.size() >= 1);
		
		BlockingItem sleep = children.last();
		assertEquals(sleep.getDuration(), 100000000, p);
		
		BlockingStats stats = bm.getBlockingStatsForTask(foundTask);
		HashMap<FileDescriptor, BlockingStatsElement<FileDescriptor>> fdStats = stats.getFileDescriptorStats();
		assertEquals(1, fdStats.size());
		
		FileDescriptor fd = (FileDescriptor) fdStats.keySet().toArray()[0];
		assertTrue(fd instanceof SocketInet);
		SocketInet sock = (SocketInet) fd;
		assertEquals(9876, sock.getDstPort());
	}

	//@Test
	public void testFDWaitingStats() throws JniException {
		String trace = "ioburst-512";
		File file = new File(Path.getTraceDir(), trace);
		// make sure we have this trace
		assertTrue("Missing trace " + trace, file.isDirectory());

		String tracePath = file.getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		BlockingModel bm = model.getBlockingModel();
		Task foundTask = model.getLatestTaskByCmdBasename("ioburst");
		BlockingStats stats = bm.getBlockingStatsForTask(foundTask);
		HashMap<FileDescriptor, BlockingStatsElement<FileDescriptor>> fdStats = stats.getFileDescriptorStats();
		assertEquals(1, fdStats.size());
		
		FileDescriptor fd = (FileDescriptor) fdStats.keySet().toArray()[0];
		assertTrue(fd instanceof DiskFile);
		DiskFile data = (DiskFile) fd;
		assertEquals("tmp.data", data.getFilename());
	}

	@Test
	public void testCpuAccountingWaitingStats() throws JniException {
		String trace = "rpc-hog-100ms";
		File file = new File(Path.getTraceDir(), trace);
		// make sure we have this trace
		assertTrue("Missing trace " + trace, file.isDirectory());

		String tracePath = file.getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		TraceReader readerTrace = new TraceReader(tracePath);
		TraceEventHandlerProcess handler = new TraceEventHandlerProcess();
		readerTrace.register(handler);
		readerTrace.process();
		ResourceUsage<Long> cpuStats = handler.getUsageStats();
		
		BlockingModel bm = model.getBlockingModel();
		Task foundTask = model.getLatestTaskByCmdBasename("clihog");
		BlockingStats stats = bm.getBlockingStatsForTask(foundTask);
		HashMap<FileDescriptor, BlockingStatsElement<FileDescriptor>> fdStats = stats.getFileDescriptorStats();
		assertEquals(1, fdStats.size());
		
		FileDescriptor fd = (FileDescriptor) fdStats.keySet().toArray()[0];
		assertTrue(fd instanceof SocketInet);
		SocketInet sock = (SocketInet) fd;
		assertEquals(9876, sock.getDstPort());

		Task server = model.getLatestTaskByCmdBasename("srvhog");
		HashMap<Integer, TreeSet<FileDescriptor>> serverFds = server.getFileDescriptors();
		SocketInet srvSock = (SocketInet) serverFds.get(4).last();
		assertEquals(9876, srvSock.getSrcPort());
		
		// client read blocking
		TreeSet<BlockingItem> items = bm.getBlockingItemsForTask(foundTask);
		BlockingItem read = items.last();
		double p = 10000000;
		assertEquals(read.getDuration(), 100000000, p);
		
		// server is busy and never block
		assertTrue(read.getChildren(model).isEmpty());
		
		CpuAccountingItem cpuAccountingItem = new CpuAccountingItem(read.getTask());
		double selfTime = cpuAccountingItem.getSelfTime(model, cpuStats);
		double subTime = cpuAccountingItem.getSubtaskTime(model, cpuStats);
		assertEquals(0.0, selfTime, p);
		// FIXME: success of this tests depends on the precision of cpu sampling
		// query between in the middle of a bucket should return a proportion of that bucket
		assertEquals(100000000, subTime, p);
		
		TreeSet<CpuAccountingItem> children = cpuAccountingItem.getChildren(model, cpuStats);
		assertEquals(2, children.size());
		CpuAccountingItem child = children.last();
		double selfTime2 = child.getSelfTime(model, cpuStats);
		double subTime2 = child.getSubtaskTime(model, cpuStats);
		assertEquals(100000000, selfTime2, p);
		assertEquals(0.0, subTime2, p);		
		
		TreeSet<CpuAccountingItem> empty = child.getChildren(model, cpuStats);
		assertEquals(0, empty.size());
	}
	
}

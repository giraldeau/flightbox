package org.lttng.flightbox.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.statistics.Bucket;
import org.lttng.flightbox.statistics.BucketSeries;

public class TestStatsBucket {

	static double p = 0.0001;
	
	@Test
	public void testIndexCalculation() {
		assertEquals(0, (int)Math.floor(0.1));
		assertEquals(0, (int)Math.floor(0.5));
		assertEquals(0, (int)Math.floor(0.6));
		assertEquals(1, (int)Math.floor(1.6));
	}
	
	@Test
	public void testCreateStatsBins() {
		BucketSeries s = new BucketSeries();
		assertEquals(0, s.size());
		assertEquals(0, s.getBinDuration(), p);
		assertEquals(0, s.getStartTime(), p);
		assertEquals(0, s.getEndTime(), p);
	}
	
	@Test
	public void testInitBins() {
		BucketSeries s = new BucketSeries();
		s.init(0.5, 0.8, 3);
		assertEquals(3, s.size());
		assertEquals(0.1, s.getBinDuration(), p);
		Bucket t1 = s.getIntervalByTime(0.65);
		assertEquals(0.6, t1.getStartTime(), p);
		assertEquals(0.7, t1.getEndTime(), p);
		t1.addTime(0.1, TaskState.USER);
		Bucket t2 = s.getIntervalByTime(0.75);
		t2.addTime(0.2, TaskState.USER);
		assertEquals(t1.getTime(TaskState.USER) +
					 t2.getTime(TaskState.USER),
					 s.getSum().getTime(TaskState.USER), p);
	}
	
	@Test
	public void testAddInterval() {
		TaskState mode = TaskState.USER;
		BucketSeries s = new BucketSeries();
		s.init(0.1, 0.7, 6);
		s.addInterval(0.0, 1.0, mode);
		Bucket t = s.getIntervalByTime(0.2);
		assertEquals(0.1, t.getTime(mode), p);
	}
}

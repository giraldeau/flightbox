package org.lttng.flightbox.junit.model;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;
import org.lttng.flightbox.model.IdMap;
import org.lttng.flightbox.model.IdProvider;

public class TestIdMap {

	class Dummy implements Comparable<Dummy> {
		private int id;
		private int seq;
		public Dummy(int id, int seq) {
			setId(id);
			setSeq(seq);
		}
		public void setId(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
		public void setSeq(int seq) {
			this.seq = seq;
		}
		public int getSeq() {
			return seq;
		}
		@Override
		public int compareTo(Dummy o) {
			if (o == this) return 0;
			if (o.getSeq() == this.getSeq()) return 0;
			if (this.getSeq() < o.getSeq()) return -1;
			return 1;
		}
		public String toString() {
			return String.format("[%d,%d]", id, seq);
		}
	}
	
	@Test
	public void testIdMap() {
		HashSet<Dummy> dummySet = new HashSet<Dummy>();
		int i, j, k = 0;
		int nb = 3; 
		/* create dummy objects with duplicates id and unique sequence number */
		for (i=0; i<=nb; i++) {
			for (j=0; j<i; j++) {
				dummySet.add(new Dummy(j, k++));
			}
		}
		IdMap<Dummy> idMap = new IdMap<Dummy>();
		idMap.setProvider(new IdProvider<Dummy>() {
			@Override
			public int getId(Dummy obj) {
				return obj.getId();
			}
		});
		idMap.addAll(dummySet);
		System.out.println(idMap);
		assertEquals(nb, idMap.size());
		assertEquals(k, idMap.historySize());
	}
}

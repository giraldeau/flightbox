package org.lttng.flightbox.histogram;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;

import statehistory.common.AttributeNotFoundException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;

public class TraceEventHandlerHistogramKryo extends TraceEventHandlerBase implements IHistogramHandler {

	private int nbSamples = 100;
	private int[] samples;
	private long start;
	private long end;
	private long duration;
	ObjectBuffer buffer;
	FileOutputStream outStream;
	FileInputStream inStream;
	HistogramSample sample;
	private int objectSize;
	File temp;
	int maxSamples;
	
	public TraceEventHandlerHistogramKryo() {
		super();
		hooks.add(new TraceHook());
	}

	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {
		maxSamples = 0;
		start = trace.getStartTime().getTime();
		end = trace.getEndTime().getTime();
		duration = end - start;
		// FIXME: never allocate a relative file inside handler
		// this is a coding horror
		temp = null;
		try {
			temp = File.createTempFile("history",".kryo");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (temp == null)
			return;
		Kryo kryo = new Kryo();
		buffer = new ObjectBuffer(kryo);
		sample = new HistogramSample();
		
		try {
			outStream = new FileOutputStream(temp);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		/* get the size of an object */
		ByteBuffer buf = ByteBuffer.allocateDirect(255);
		kryo.writeObjectData(buf, sample);
		objectSize = buf.position();
	}

	@Override
	public void handleComplete(TraceReader reader) {
		try {
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		maxSamples = sample.count;
	}

	public void handle_all_event(TraceReader reader, JniEvent event) {
		if (buffer == null || outStream == null)
			return;
		
		sample.t = event.getEventTime().getTime();
		sample.count++;
		buffer.writeObjectData(outStream, sample);
	}

	public int[] getSamples() throws AttributeNotFoundException {
		samples = new int[nbSamples];
		int currStateValueInt = 0;
		int prevStateValueInt = 0;
		long tCurr = 0;
		int delta = 0;
		long offset = 0;
		HistogramSample s;
		
		inStream = null;
		try {
			inStream = new FileInputStream(temp.getPath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (inStream == null)
			return samples;

		for(int i = 0; i< nbSamples; i++) {
			offset = ((i + 1) * duration) / nbSamples;
			tCurr = offset + start;
			s = searchSample(inStream, tCurr);
			currStateValueInt = s.count;
			delta = currStateValueInt - prevStateValueInt;
			samples[i] = delta;
			prevStateValueInt = currStateValueInt;
		}
		return samples;
	}

	private HistogramSample searchSample(FileInputStream inStream, long tCurr) {
		// dichotomic search in the serialized objects
		int min = 1;
		int max = maxSamples;
		int mid = 1;
		while(mid == 0) {
			
		}
		int middle = maxSamples * objectSize / 2;
		
		return null;
	}

	public HistogramSample getObjectAtOffset(int offset) {
		if (inStream == null)
			return null;
		try {
			inStream.reset();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			inStream.skip(offset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.readObject(inStream, HistogramSample.class);
	}
	
	public void setNbSamples(int s) {
		nbSamples = s;
	}

}

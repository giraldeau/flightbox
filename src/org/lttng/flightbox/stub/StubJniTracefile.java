package org.lttng.flightbox.stub;

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class StubJniTracefile extends JniTracefile {

	String traceFileName;
	long cpu;
	@Override
	public JniEvent allocateNewJniEvent(
			Jni_C_Pointer_And_Library_Id newEventPtr,
			HashMap<Integer, JniMarker> newMarkersMap,
			JniTracefile newParentTracefile) throws JniException {
		return null;
	}

	@Override
	public JniMarker allocateNewJniMarker(
			Jni_C_Pointer_And_Library_Id newMarkerPtr) throws JniException {
		return null;
	}
	public String getTracefileName() {
		return traceFileName;
	}
	public void setTracefileName(String traceFileName) {
		this.traceFileName = traceFileName;
	}
	public void setCpuNumber(long cpu) {
		this.cpu = cpu;
	}
	public long getCpuNumber() {
		return cpu;
	}
}
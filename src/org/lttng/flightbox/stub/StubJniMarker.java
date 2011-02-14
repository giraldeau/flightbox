package org.lttng.flightbox.stub;

import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class StubJniMarker extends JniMarker {

	public String markerName;
	
	@Override
	public JniMarkerField allocateNewJniMarkerField(
			Jni_C_Pointer_And_Library_Id newMarkerFieldPtr)
			throws JniException {
		return null;
	}
	
	public void setName(String name) {
		this.markerName = name;
	}
	
	public String getName() {
		return markerName;
	}
	
}
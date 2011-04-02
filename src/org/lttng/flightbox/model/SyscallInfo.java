package org.lttng.flightbox.model;

import java.util.EnumMap;
import java.util.Map;

public class SyscallInfo extends StateInfo {

	public enum Field {
		PREV_PID, NEXT_PID, PARENT_PID, CHILD_PID,
		PID, TGID, CHILD_TGID, FILENAME, IP, FD,
		STATE, CPU_ID
	}

	private int syscallId;
	private int retCode;
	private Map<Field, Object> fieldInfo;

	public SyscallInfo() {
		super();
	}

	public void setField(Field key, Object value) {
		if (fieldInfo == null)
			fieldInfo = new EnumMap<Field, Object>(Field.class);
		fieldInfo.put(key, value);
	}

	public Object getField(Field key) {
		return fieldInfo.get(key);
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

	public int getRetCode() {
		return retCode;
	}

	public void setSyscallId(int syscallId) {
		this.syscallId = syscallId;
	}

	public int getSyscallId() {
		return syscallId;
	}

	@Override
	public void reset() {
		if (fieldInfo != null)
			fieldInfo.clear();
		syscallId = 0;
		retCode = 0;
	}
}

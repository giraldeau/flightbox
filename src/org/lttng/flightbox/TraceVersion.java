package org.lttng.flightbox;

public class TraceVersion {

	public short major; 
	public short minor;
	
	public short getMajor() {
		return major;
	}
	public void setMajor(short major) {
		this.major = major;
	}
	public short getMinor() {
		return minor;
	}
	public void setMinor(short minor) {
		this.minor = minor;
	}
	public String toString() {
		return this.major + "." + this.minor;
	}
}

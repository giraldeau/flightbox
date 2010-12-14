package org.lttng.flightbox.cpu;

public class KernelProcess implements Comparable {

	Long pid;
	String cmd;
	
	public KernelProcess(long pid, String cmd) {
		this.pid = pid;
		this.cmd = cmd;
	}
	public KernelProcess() {
	}
	
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	
	public boolean equals(Object other) {
		if (other instanceof KernelProcess) {
			KernelProcess p = (KernelProcess) other;
			if (p.pid == this.pid) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return this.pid.hashCode();
	}
	
	public int compareTo(Object other) {
		if (other instanceof KernelProcess) {
			KernelProcess p = (KernelProcess) other;
			return this.pid.compareTo(p.pid);
		}
		return -1;
	}

	public String toString() {
		return "pid=" + pid + " cmd=" + cmd; 
	}
}

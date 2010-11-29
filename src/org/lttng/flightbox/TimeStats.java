package org.lttng.flightbox;

public class TimeStats {

	private double user; 
	private double irq;
	private double trap;
	private double syscall;
	private double t1;
	private double t2;
	
	public TimeStats() {
		this(0,0);
	}
	
	public TimeStats(double t1, double t2) {
		this.setStartTime(t1);
		this.setEndTime(t2);
	}
	
	public double getUser() {
		return user;
	}
	
	public void addUser(double user) {
		this.user += user;
	}
	
	public double getIrq() {
		return irq;
	}
	
	public void addIrq(double irq) {
		this.irq += irq;
	}
	
	public double getTrap() {
		return trap;
	}
	
	public void addTrap(double trap) {
		this.trap += trap;
	}
	
	public double getSyscall() {
		return syscall;
	}
	
	public void addSyscall(double syscall) {
		this.syscall += syscall;
	}
	
	public double getStartTime() {
		return t1;
	}
	
	public void setStartTime(double t1) {
		this.t1 = t1;
	}
	
	public double getEndTime() {
		return t2;
	}
	
	public void setEndTime(double t2) {
		this.t2 = t2;
	}
	
	public double getSystem() {
		return irq + trap + syscall;
	}
	
	public double getTotal() {
		return irq + trap + syscall + user;
	}
	
	public double getDuration() {
		return (t2 - t1);
	}
	
	public double getIdle() {
		return getDuration() - getTotal();
	}
	
	public double getTotalAvg() {
		return getTotal() / getDuration();
	}
	
	public double getUserAvg() {
		return getUser() / getDuration();
	}
	
	public double getTrapAvg() {
		return getTrap() / getDuration();
	}
	
	public double getIrqAvg() {
		return getIrq() / getDuration();
	}
	
	public double getSyscallAvg() {
		return getSyscall() / getDuration();
	}
	
	public double getIdleAvg() {
		return getIdle() / getDuration();
	}
	
	public double getSystemAvg() {
		return getSystem() / getDuration();
	}
	
	public void add(TimeStats other) {
		if (other.getStartTime() < t1){
			this.t1 = other.getStartTime();
		}
		if (other.getEndTime() > t2) {
			this.t2 = other.getEndTime();
		}
		user += other.getUser();
		irq += other.getIrq();
		trap += other.getTrap();
		syscall += other.getSyscall();
	}
}

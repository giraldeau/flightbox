package org.lttng.flightbox.model;

public class IPv4Con implements Comparable<IPv4Con> {

	private long srcAddr;
	private long dstAddr;
	private int srcPort;
	private int dstPort;
	
	public IPv4Con() {
	}
	
	public IPv4Con(long srcAddr, int srcPort, long dstAddr, int dstPort) {
		setSrcAddr(srcAddr);
		setSrcPort(srcPort);
		setDstAddr(dstAddr);
		setDstPort(dstPort);
	}
	
	public long getSrcAddr() {
		return srcAddr;
	}
	public void setSrcAddr(long srcAddr) {
		this.srcAddr = srcAddr;
	}
	public long getDstAddr() {
		return dstAddr;
	}
	public void setDstAddr(long dstAddr) {
		this.dstAddr = dstAddr;
	}
	public int getSrcPort() {
		return srcPort;
	}
	public void setSrcPort(int srcPort) {
		this.srcPort = srcPort;
	}
	public int getDstPort() {
		return dstPort;
	}
	public void setDstPort(int dstPort) {
		this.dstPort = dstPort;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o instanceof IPv4Con) {
			IPv4Con other = (IPv4Con) o;
			if (other.srcAddr == this.srcAddr &&
				other.dstAddr == this.dstAddr &&
				other.srcPort == this.srcPort &&
				other.dstPort == this.dstPort)
				return true;
		}
		return false;
	}

	/**
	 * returns true if the socket is complementary
	 * @param other
	 * @return
	 */
	public boolean isComplement(IPv4Con other) {
		if (other.srcAddr == this.dstAddr &&
			other.dstAddr == this.srcAddr &&
			other.srcPort == this.dstPort &&
			other.dstPort == this.srcPort)
			return true;
		return false;
	}
	
	public IPv4Con getComplement() {
		return new IPv4Con(getDstAddr(), getDstPort(), getSrcAddr(), getSrcPort());
	}
	
	@Override
	public int hashCode() {
		int x = (int)(srcAddr ^ srcAddr >>> 32);
		int y = (int)(dstAddr ^ dstAddr >>> 32);
		return x * 2 + y * 3 + srcPort * 4 + dstPort * 5;
	}
	
	public boolean isSet() {
		return (srcAddr != 0) && (dstAddr != 0) && (srcPort != 0) && (dstPort != 0);
	}
	@Override
	public int compareTo(IPv4Con arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	public static final byte[] intToByteArray(int value) {
		return new byte[] {
			(byte)(value >>> 24),
			(byte)(value >>> 16),
			(byte)(value >>> 8),
			(byte)(value) };
	}
	
	public static String formatIPv4(long addr) {
		StringBuilder str = new StringBuilder();
		byte[] b = intToByteArray((int) addr);
		str.append(String.format("%d.", (b[0] & 0xFF)));
		str.append(String.format("%d.", (b[1] & 0xFF)));
		str.append(String.format("%d.", (b[2] & 0xFF)));
		str.append(String.format("%d",  (b[3] & 0xFF)));
		return str.toString();
	}
	
	public String toString() {
		return String.format("%s:%d->%s:%d", formatIPv4(srcAddr), srcPort, formatIPv4(dstAddr), dstPort);
	}
}

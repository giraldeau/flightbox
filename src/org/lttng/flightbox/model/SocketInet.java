package org.lttng.flightbox.model;

public class SocketInet extends FileDescriptor {

	/* socket types */
	public static final int SOCK_STREAM    = 1;
	public static final int SOCK_DGRAM     = 2;
	public static final int SOCK_RAW       = 3;
	public static final int SOCK_RDM       = 4;
	public static final int SOCK_SEQPACKET = 5;
	public static final int SOCK_DCCP      = 6;
	public static final int SOCK_PACKET    = 10;

	/*
	public static final int SOCK_CLOEXEC   = 2000000;
	public static final int SOCK_NONBLOCK  = 4000;
	*/
	public enum Type {
		SOCK_STREAM(1),
		SOCK_DGRAM(2),
		SOCK_RAW(3),
		SOCK_RDM(4),
		SOCK_SEQPACKET(5),
		SOCK_DCCP(6),
		SOCK_PACKET(10);

		private final int i;
		private Type(int x) { i = x; }
		public int getValue() { return i; }
	}

	/* protocol families */
	public static final int AF_UNSPEC       = 0;
	public static final int AF_LOCAL        = 1;
	public static final int AF_UNIX         = 1;
	public static final int AF_FILE         = 1;
	public static final int AF_INET         = 2;
	public static final int AF_AX25         = 3;
	public static final int AF_IPX          = 4;
	public static final int AF_APPLETALK    = 5;
	public static final int AF_NETROM       = 6;
	public static final int AF_BRIDGE       = 7;
	public static final int AF_ATMPVC       = 8;
	public static final int AF_X25          = 9;
	public static final int AF_INET6        = 10;
	public static final int AF_ROSE         = 11;
	public static final int AF_DECnet       = 12;
	public static final int AF_NETBEUI      = 13;
	public static final int AF_SECURITY     = 14;
	public static final int AF_KEY          = 15;
	public static final int AF_NETLINK      = 16;
	public static final int AF_ROUTE        = 16;
	public static final int AF_PACKET       = 17;
	public static final int AF_ASH          = 18;
	public static final int AF_ECONET       = 19;
	public static final int AF_ATMSVC       = 20;
	public static final int AF_RDS          = 21;
	public static final int AF_SNA          = 22;
	public static final int AF_IRDA         = 23;
	public static final int AF_PPPOX        = 24;
	public static final int AF_WANPIPE      = 25;
	public static final int AF_LLC          = 26;
	public static final int AF_CAN          = 29;
	public static final int AF_TIPC         = 30;
	public static final int AF_BLUETOOTH    = 31;
	public static final int AF_IUCV         = 32;
	public static final int AF_RXRPC        = 33;
	public static final int AF_ISDN         = 34;
	public static final int AF_PHONET       = 35;
	public static final int AF_IEEE802154   = 36;
	public static final int AF_MAX          = 37;

	private int type;
	private int family;
	private IPv4Con ip;
	private int protocol;
	private boolean isClient;
	private long pointer;
	private long send;
	private long recv;

	public SocketInet() {
		this(new IPv4Con());
	}
	
	public SocketInet(IPv4Con ip) {
		setIp(ip);
		setType(AF_INET);
	}

	public SocketInet(long srcAddr, int srcPort, long dstAddr, int dstPort) {
		this(new IPv4Con(srcAddr, srcPort, dstAddr, dstPort));
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getFamily() {
		return family;
	}
	public void setFamily(int family) {
		this.family = family;
	}
	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}
	public int getProtocol() {
		return protocol;
	}

	@Override
	public String toString() {
		return String.format("[%d,%s]", getFd(), ip.toString());
	}
	public void setClient(boolean isXmit) {
		this.isClient = isXmit;
	}
	public boolean isClient() {
		return isClient;
	}

	public void setPointer(long pointer) {
		this.pointer = pointer;
	}
	public long getPointer() {
		return pointer;
	}
	public void incrementSend(long len) {
		this.send += len;
	}
	public long getSend() {
		return this.send;
	}
	public void setSend(long send) {
		this.send = send;
	}
	public void incrementRecv(long len) {
		this.recv += len;
	}
	public long getRecv() {
		return this.recv;
	}
	public void setRecv(long recv) {
		this.recv = recv;
	}

	public IPv4Con getIp() {
		return ip;
	}
	public void setIp(IPv4Con ip) {
		this.ip = ip;
	}

	public boolean isConnected(SocketInet other) {
		if (other.getType() == this.getType() &&
			getIp().isComplement(other.getIp()))
			return true;
		return false;
	}
}

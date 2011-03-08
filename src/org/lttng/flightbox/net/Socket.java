package org.lttng.flightbox.net;

public class Socket {
	public enum State {
		ESTABLISHED, SYN_SENT, SYN_RECV, FIN_WAIT1, FIN_WAIT2, TIME_WAIT,
		CLOSE, CLOSE_WAIT, LAST_ACK, LISTEN, CLOSING, UNKNOWN
	}
}

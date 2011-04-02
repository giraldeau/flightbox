package org.lttng.flightbox.model;

public class IRQInfo extends StateInfo {

	private int irqId;

	public void setIRQId(int irqId) {
		this.irqId = irqId;
	}

	public int getIRQId() {
		return irqId;
	}

	@Override
	public void reset() {
		irqId = 0;
	}

}

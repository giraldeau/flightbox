package org.lttng.flightbox.model;

public abstract class SystemResource implements ISystemResource {

	SystemModel parent;

	public SystemModel getParent() {
		return parent;
	}

	@Override
	public void setParent(SystemModel model) {
		this.parent = model;
	}
}

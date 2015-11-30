package io.vertigo.commons.eventbus.data;

import io.vertigo.commons.eventbus.Event;

public abstract class ColorEvent implements Event {
	private final String color;

	ColorEvent(final String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}
}

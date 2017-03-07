package io.vertigo.commons.analytics.data;

import io.vertigo.commons.impl.analytics.AProcess;
import io.vertigo.commons.impl.analytics.AProcessConnectorPlugin;

public class TestAProcessConnectorPlugin implements AProcessConnectorPlugin {
	public static int count = 0;
	public static String lastChannel;
	public static Double lastPrice;

	@Override
	public void add(final AProcess process) {
		count++;
		lastChannel = process.getType();
		lastPrice = process.getMeasures().get("price");
	}

	public static int getCount() {
		return count;
	}

	public static String getLastChannel() {
		return lastChannel;
	}

	public static void reset() {
		count = 0;
		lastChannel = null;
		lastPrice = null;
	}

	public static Double getLastPrice() {
		return lastPrice;
	}
}

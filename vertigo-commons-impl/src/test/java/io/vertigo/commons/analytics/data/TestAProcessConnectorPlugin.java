package io.vertigo.commons.analytics.data;

import io.vertigo.commons.impl.analytics.AProcess;
import io.vertigo.commons.impl.analytics.AProcessConnectorPlugin;

public class TestAProcessConnectorPlugin implements AProcessConnectorPlugin {
	public static int count = 0;
	public static String lastProcessType;
	public static Double lastPrice;

	@Override
	public void add(final AProcess process) {
		count++;
		lastProcessType = process.getType();
		lastPrice = process.getMeasures().get("price");
	}

	public static int getCount() {
		return count;
	}

	public static String getLastProcessType() {
		return lastProcessType;
	}

	public static void reset() {
		count = 0;
		lastProcessType = null;
		lastPrice = null;
	}

	public static Double getLastPrice() {
		return lastPrice;
	}
}

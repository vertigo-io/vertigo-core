package io.vertigo.commons.analytics.data;

import io.vertigo.commons.impl.analytics.AProcess;
import io.vertigo.commons.impl.analytics.AnalyticsConnectorPlugin;

public class TestAProcessConnectorPlugin implements AnalyticsConnectorPlugin {
	public static int count = 0;
	public static String lastCategory;
	public static Double lastPrice;

	@Override
	public void add(final AProcess process) {
		count++;
		lastCategory = process.getCategory();
		lastPrice = process.getMeasures().get("price");
	}

	public static int getCount() {
		return count;
	}

	public static String getLastcategory() {
		return lastCategory;
	}

	public static void reset() {
		count = 0;
		lastCategory = null;
		lastPrice = null;
	}

	public static Double getLastPrice() {
		return lastPrice;
	}
}

package io.vertigo.commons.plugins.analytics.log;

import java.util.Collections;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertigo.commons.impl.analytics.AProcess;
import io.vertigo.commons.impl.analytics.AnalyticsConnectorPlugin;

/**
 * Processes connector which only use a log4j logger.
 * @author mlaroche,pchretien,npiedeloup
 */
public final class LoggerAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin {
	private static final Gson GSON = new GsonBuilder().create();

	/** {@inheritDoc} */
	@Override
	public void add(final AProcess process) {
		final Logger logger = Logger.getLogger(process.getCategory());
		if (logger.isInfoEnabled()) {
			final String json = GSON.toJson(Collections.singletonList(process));
			logger.info(json);
		}
	}

}

package io.vertigo.commons.plugins.analytics.log;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

import io.vertigo.commons.impl.analytics.AProcess;
import io.vertigo.commons.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.lang.Assertion;

/**
 * This connector analyses the process and calculates duration and count.
 * @author mlaroche,pchretien
 */
public final class SmartLoggerAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin {

	private final Optional<String> aggregatedByOpt;
	private final Long durationThreshold;

	/**
	 * Constructor.
	 * @param aggregatedByOpt optional param for aggrating subprocesses results with specific category
	 * @param durationThresholdOpt optional param for setting the error level to log in error
	 */
	@Inject
	public SmartLoggerAnalyticsConnectorPlugin(@Named("aggregatedBy") final Optional<String> aggregatedByOpt,
			@Named("durationThreshold") final Optional<Long> durationThresholdOpt) {
		Assertion.checkNotNull(aggregatedByOpt);
		Assertion.checkNotNull(durationThresholdOpt);
		//---
		this.aggregatedByOpt = aggregatedByOpt;
		// see Jakob Nielsen dhttps://www.nngroup.com/articles/response-times-3-important-limits/
		durationThreshold = durationThresholdOpt.orElse(1000L);
	}

	/** {@inheritDoc} */
	@Override
	public void add(final AProcess process) {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("name", process.getName());
		jsonObject.addProperty("durationMillis", process.getDurationMillis());

		if (aggregatedByOpt.isPresent()) {
			final AggregatedResult result = new AggregatedResult();
			buildCountAndFullDurationByCategory(process, aggregatedByOpt.get(), result);
			if (result.count != 0) {
				final JsonObject aggregatedObject = new JsonObject();
				aggregatedObject.addProperty("count", result.count);
				aggregatedObject.addProperty("durationMillis", result.duration);
				//--
				jsonObject.add(aggregatedByOpt.get(), aggregatedObject);
			}
		}

		final Logger logger = Logger.getLogger(process.getCategory());
		if (process.getDurationMillis() > durationThreshold) {
			logger.error(jsonObject.toString());
		} else if (logger.isInfoEnabled()) {
			logger.info(jsonObject.toString());
		}
	}

	private static void buildCountAndFullDurationByCategory(final AProcess process, final String category, final AggregatedResult aggregatedResult) {
		process.getSubProcesses()
				.stream()
				.filter(subprocess -> category.equals(subprocess.getCategory()))
				.forEach(subprocess -> {
					aggregatedResult.count++;
					aggregatedResult.duration += subprocess.getDurationMillis();
				});

		//---
		process.getSubProcesses()
				.stream()
				.filter(subprocess -> !category.equals(subprocess.getCategory()))
				.forEach(subprocess -> buildCountAndFullDurationByCategory(subprocess, category, aggregatedResult));

	}

	static class AggregatedResult {
		int count;
		long duration;
	}
}

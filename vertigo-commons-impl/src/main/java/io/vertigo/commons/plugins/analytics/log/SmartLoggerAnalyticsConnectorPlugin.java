/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version of the library,
 * but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
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
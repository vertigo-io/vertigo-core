package io.vertigo.database.impl.timeseries;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.database.timeseries.ClusteredMeasure;
import io.vertigo.database.timeseries.DataFilter;
import io.vertigo.database.timeseries.Measure;
import io.vertigo.database.timeseries.TimeFilter;
import io.vertigo.database.timeseries.TimeSeriesDataBaseManager;
import io.vertigo.database.timeseries.TimedDatas;
import io.vertigo.lang.Assertion;

/**
 * @author mlaroche
 *
 */
public class TimeSeriesDataBaseManagerImpl implements TimeSeriesDataBaseManager {

	private final TimeSeriesPlugin timeSeriesPlugin;

	@Inject
	public TimeSeriesDataBaseManagerImpl(
			final TimeSeriesPlugin timeSeriesPlugin,
			final @Named("dbNames") Optional<String> dbNamesOpt) {
		Assertion.checkNotNull(timeSeriesPlugin);
		Assertion.checkNotNull(dbNamesOpt);
		//---
		this.timeSeriesPlugin = timeSeriesPlugin;
		if (dbNamesOpt.isPresent()) {
			timeSeriesPlugin.createDatabases(Arrays.asList(dbNamesOpt.get().split(";")));
		}
	}

	@Override
	public void insertMeasure(final String dbName, final Measure measure) {
		timeSeriesPlugin.insertMeasure(dbName, measure);

	}

	@Override
	public TimedDatas getTimeSeries(final String dbName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.checkArgNotEmpty(dbName);
		Assertion.checkNotNull(measures);
		Assertion.checkNotNull(dataFilter);
		Assertion.checkNotNull(timeFilter.getDim());// we check dim is not null because we need it
		//---
		return timeSeriesPlugin.getTimeSeries(dbName, measures, dataFilter, timeFilter);

	}

	@Override
	public TimedDatas getClusteredTimeSeries(final String dbName, final ClusteredMeasure clusteredMeasure, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.checkArgNotEmpty(dbName);
		Assertion.checkNotNull(dataFilter);
		Assertion.checkNotNull(timeFilter);
		Assertion.checkNotNull(timeFilter.getDim()); // we check dim is not null because we need it
		Assertion.checkNotNull(clusteredMeasure);
		//---
		Assertion.checkArgNotEmpty(clusteredMeasure.getMeasure());
		Assertion.checkNotNull(clusteredMeasure.getThresholds());
		Assertion.checkState(!clusteredMeasure.getThresholds().isEmpty(), "For clustering the measure '{0}' you need to provide at least one threshold", clusteredMeasure.getMeasure());
		//we use the natural order
		clusteredMeasure.getThresholds().sort(Comparator.naturalOrder());
		//---
		return timeSeriesPlugin.getClusteredTimeSeries(dbName, clusteredMeasure, dataFilter, timeFilter);
	}

	@Override
	public TimedDatas getTabularData(final String dbName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final boolean keepTime, final String... groupBy) {
		return timeSeriesPlugin.getTabularData(dbName, measures, dataFilter, timeFilter, keepTime, groupBy);
	}

	@Override
	public TimedDatas getTops(final String dbName, final String measure, final DataFilter dataFilter, final TimeFilter timeFilter, final String groupBy, final int maxRows) {
		return timeSeriesPlugin.getTops(dbName, measure, dataFilter, timeFilter, groupBy, maxRows);
	}

	@Override
	public List<String> getTagValues(final String dbName, final String measurement, final String tag) {
		return timeSeriesPlugin.getTagValues(dbName, measurement, tag);
	}

}

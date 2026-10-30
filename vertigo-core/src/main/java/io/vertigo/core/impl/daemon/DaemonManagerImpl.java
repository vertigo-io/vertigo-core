/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.impl.daemon;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.health.HealthChecked;
import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.analytics.metric.Metrics;
import io.vertigo.core.daemon.DaemonManager;
import io.vertigo.core.daemon.DaemonScheduled;
import io.vertigo.core.daemon.DaemonStat;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.component.AspectPlugin;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.ClassUtil;

/**
 * Manager of all the daemons.
 *
 * @author mlaroche, pchretien, npiedeloup
 */
public final class DaemonManagerImpl implements DaemonManager, Activeable, SimpleDefinitionProvider {

	private static final int DEFAULT_THREAD_POOL_SIZE = 2;
	private static final int DEFAULT_MIN_DURATION_IN_SECONDS = 120;
	private static final int DEFAULT_MAX_DURATION_IN_SECONDS = 2 * 3600;
	/** A daemon has a problem beyond 4 x its period (bounded by min and max durations). */
	private static final int PERIOD_MULTIPLIER = 4;

	private final DaemonExecutor daemonExecutor;
	private final long minDurationMillis;
	private final long maxDurationMillis;
	private volatile boolean active;

	/**
	 * Construct an instance of DaemonManagerImpl.
	 *
	 * @param analyticsManager AnalyticsManager
	 * @param threadPoolSizeOpt thread pool size (default 2)
	 * @param daemonMinDurationInSecondsOpt minimum duration before judging that a daemon has a problem, in seconds (default 120)
	 * @param daemonMaxDurationInSecondsOpt maximum duration a daemon may run, in seconds (default 7200)
	 */
	@Inject
	public DaemonManagerImpl(final AnalyticsManager analyticsManager,
			@ParamValue("threadPoolSize") final Optional<Integer> threadPoolSizeOpt,
			@ParamValue("daemonMinDurationInSeconds") final Optional<Integer> daemonMinDurationInSecondsOpt,
			@ParamValue("daemonMaxDurationInSeconds") final Optional<Integer> daemonMaxDurationInSecondsOpt) {
		final int threadPoolSize = threadPoolSizeOpt.orElse(DEFAULT_THREAD_POOL_SIZE);
		final int minDurationInSeconds = daemonMinDurationInSecondsOpt.orElse(DEFAULT_MIN_DURATION_IN_SECONDS);
		final int maxDurationInSeconds = daemonMaxDurationInSecondsOpt.orElse(DEFAULT_MAX_DURATION_IN_SECONDS);
		// values passed as String : MessageFormat would add locale-dependent thousands separators to numbers
		Assertion.check()
				.isNotNull(analyticsManager)
				.isTrue(threadPoolSize >= 1, "threadPoolSize ({0}) must be at least 1", String.valueOf(threadPoolSize))
				.isTrue(minDurationInSeconds >= 1, "daemonMinDurationInSeconds ({0}) must be at least 1", String.valueOf(minDurationInSeconds))
				.isTrue(minDurationInSeconds <= maxDurationInSeconds, "daemonMinDurationInSeconds ({0}) must not exceed daemonMaxDurationInSeconds ({1}) (default min {2}, default max {3})",
						String.valueOf(minDurationInSeconds), String.valueOf(maxDurationInSeconds), String.valueOf(DEFAULT_MIN_DURATION_IN_SECONDS), String.valueOf(DEFAULT_MAX_DURATION_IN_SECONDS));
		//---
		minDurationMillis = minDurationInSeconds * 1000L;
		maxDurationMillis = maxDurationInSeconds * 1000L;
		daemonExecutor = new DaemonExecutor(analyticsManager, threadPoolSize);
		//--
		Node.getNode().registerPreActivateFunction(this::startAllDaemons);
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		// we need to unwrap the component to scan the real class and not the enhanced version
		final AspectPlugin aopPlugin = Node.getNode().getNodeConfig().bootConfig().aspectPlugin();
		return Node.getNode().getComponentSpace().keySet()
				.stream()
				.flatMap(id -> createDaemonDefinitions(Node.getNode().getComponentSpace().resolve(id, CoreComponent.class), aopPlugin).stream())
				.toList();
	}

	private static List<DaemonDefinition> createDaemonDefinitions(final CoreComponent component, final AspectPlugin aopPlugin) {
		return Stream.of(aopPlugin.unwrap(component).getClass().getMethods())
				.filter(method -> method.isAnnotationPresent(DaemonScheduled.class))
				.map(
						method -> {
							Assertion.check().isTrue(method.getParameterTypes().length == 0,
									"Method {0} on component {1} cannot have any parameter to be used as a daemon", method.getName(), component.getClass().getName());
							//---
							final DaemonScheduled daemonSchedule = method.getAnnotation(DaemonScheduled.class);
							return new DaemonDefinition(
									daemonSchedule.name(),
									() -> () -> ClassUtil.invoke(component, method),
									daemonSchedule.periodInSeconds(),
									daemonSchedule.analytics());
						})
				.toList();

	}

	/** {@inheritDoc} */
	@Override
	public List<DaemonStat> getStats() {
		return List.copyOf(daemonExecutor.getStats());
	}

	/**
	 * Gets the metrics of the daemon pool : its size, and the executions over a window since the previous call (the reader resets the window).
	 * Counts are sums, durations are averages in millis (omitted when no execution in the window).
	 * Called hourly by the analytics daemon : any other caller of AnalyticsManager.getMetrics() consumes the window.
	 *
	 * @return the daemon pool metrics
	 */
	@Metrics
	public List<Metric> getDaemonPoolMetrics() {
		return daemonExecutor.getPoolMetrics();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		active = true;
		daemonExecutor.start();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		// clear active before shutting the pool: a shut-down pool while the manager is active is a failure (RED),
		// a shut-down pool during a graceful stop is expected
		active = false;
		daemonExecutor.stop();
	}

	/**
	 * Démarre un démon.
	 * Celui-ci aura été préalablement enregistré.
	 * Il sera lancé puis réexécuté périodiquement.
	 * L'instance du démon est créée par injection de dépendances.
	 *
	 * @param daemonDefinition Le démon à lancer.
	 */
	private void startDaemon(final DaemonDefinition daemonDefinition) {
		Assertion.check().isNotNull(daemonDefinition);
		// -----
		daemonExecutor.scheduleDaemon(daemonDefinition);
	}

	/**
	 * Démarre l'ensemble des démons préalablement enregistré dans le spaceDefinition.
	 */
	private void startAllDaemons() {
		Node.getNode().getDefinitionSpace().getAll(DaemonDefinition.class).stream()
				.forEach(this::startDaemon);
	}

	// Health design rule: these checks judge the daemon ENGINE (pool, scheduling pace), never the business meaning of a daemon.
	// Contract: a daemon run lasts less than its period, and never more than daemonMaxDurationInSeconds (2h by default):
	// beyond, it should be an Orchestra job. limit(period) = min(max(4 x period, daemonMinDurationInSeconds), daemonMaxDurationInSeconds).
	// RED only when the engine itself is broken (a dead daemon, a shut-down pool, a daemon waiting 2 x max for a free thread):
	// the RED status reaches the readiness probe. A daemon running too long or a failing daemon is only YELLOW.
	// Daemons are never interrupted. Checks never log: statuses are pushed to the analytics connectors.

	/**
	 * Checks the state of each daemon : RED if a daemon is dead (its schedule was cancelled by a fatal Error),
	 * YELLOW if a daemon has been running for more than its limit.
	 *
	 * @return the health measure
	 */
	@HealthChecked(name = "daemonsAlive", feature = "daemons")
	public HealthMeasure checkDaemonsAlive() {
		return checkDaemonsAlive(daemonExecutor.getStats(), System.currentTimeMillis(), minDurationMillis, maxDurationMillis);
	}

	/**
	 * Checks the pace of the daemon engine : YELLOW if a daemon has been waiting for a free thread for more than its limit,
	 * RED if it has been waiting for more than twice the max duration, or if the pool is shut down while the manager is active.
	 *
	 * @return the health measure
	 */
	@HealthChecked(name = "poolUtilization", feature = "daemons")
	public HealthMeasure checkDaemonPoolUtilization() {
		// read isShutdown() before active: stop() clears active (volatile) before shutting the pool,
		// so a check that sees the pool shut down is guaranteed to read active == false on a graceful stop
		final boolean shutdownWhileActive = daemonExecutor.isShutdown() && active;
		return checkDaemonPoolUtilization(daemonExecutor.getStats(), System.currentTimeMillis(), minDurationMillis, maxDurationMillis, shutdownWhileActive);
	}

	/**
	 * Checks the daemons in failure (from a failure until two consecutive successes) : YELLOW, never RED.
	 *
	 * @return the health measure
	 */
	@HealthChecked(name = "lastExecs", feature = "daemons")
	public HealthMeasure checkDaemonsExecs() {
		final List<String> inFailureDaemonNames = daemonExecutor.getStats().stream()
				.filter(DaemonStatImpl::isInFailure)
				.map(DaemonStat::getDaemonName)
				.toList();
		if (!inFailureDaemonNames.isEmpty()) {
			return HealthMeasure.builder()
					.withYellowStatus("Daemon(s) " + String.join(", ", inFailureDaemonNames) + " in failure")
					.build();
		}
		return HealthMeasure.builder().withGreenStatus().build();
	}

	/**
	 * Problem threshold of a daemon : min(max(4 x period, minDuration), maxDuration).
	 *
	 * @param periodInSeconds daemon period
	 * @param minDurationMillis minimum duration before judging that a daemon has a problem
	 * @param maxDurationMillis maximum duration a daemon may run
	 * @return the threshold in millis
	 */
	static long limitMillis(final int periodInSeconds, final long minDurationMillis, final long maxDurationMillis) {
		return Math.min(Math.max(periodInSeconds * 1000L * PERIOD_MULTIPLIER, minDurationMillis), maxDurationMillis);
	}

	static HealthMeasure checkDaemonsAlive(final List<DaemonStatImpl> daemonStats, final long nowMillis, final long minDurationMillis, final long maxDurationMillis) {
		final List<String> deadDaemonNames = daemonStats.stream()
				.filter(DaemonStatImpl::isDead)
				.map(DaemonStat::getDaemonName)
				.toList();
		if (!deadDaemonNames.isEmpty()) {
			return HealthMeasure.builder()
					.withRedStatus("Daemon(s) " + String.join(", ", deadDaemonNames) + " are dead : their periodic schedule was cancelled by a fatal Error")
					.build();
		}
		//---
		for (final DaemonStatImpl daemonStat : daemonStats) {
			final long runningMillis = daemonStat.getStatus() == DaemonStat.Status.running ? nowMillis - daemonStat.getStartMillis() : 0L;
			final long limitMillis = limitMillis(daemonStat.getDaemonPeriodInSecond(), minDurationMillis, maxDurationMillis);
			if (runningMillis > limitMillis) {
				return HealthMeasure.builder()
						.withYellowStatus("Daemon " + daemonStat.getDaemonName() + " running for " + runningMillis / 1000 + "s (limit " + limitMillis / 1000 + "s)")
						.build();
			}
		}
		return HealthMeasure.builder().withGreenStatus().build();
	}

	static HealthMeasure checkDaemonPoolUtilization(final List<DaemonStatImpl> daemonStats, final long nowMillis, final long minDurationMillis, final long maxDurationMillis,
			final boolean shutdownWhileActive) {
		if (shutdownWhileActive) {
			return HealthMeasure.builder()
					.withRedStatus("Daemon pool is shut down, no daemon can run anymore")
					.build();
		}
		//---
		HealthMeasure healthMeasure = HealthMeasure.builder().withGreenStatus().build();
		for (final DaemonStatImpl daemonStat : daemonStats) {
			// a running (or dead) daemon owns its thread : it is not waiting
			final long waitingMillis = daemonStat.getStatus() == DaemonStat.Status.pending && !daemonStat.isDead() ? nowMillis - daemonStat.getDueMillis() : 0L;
			final long limitMillis = limitMillis(daemonStat.getDaemonPeriodInSecond(), minDurationMillis, maxDurationMillis);
			if (waitingMillis > 2 * maxDurationMillis) {
				return HealthMeasure.builder()
						.withRedStatus("Daemon " + daemonStat.getDaemonName() + " waiting for a free thread for " + waitingMillis / 1000 + "s (max " + 2 * maxDurationMillis / 1000
								+ "s) : the daemon engine is broken")
						.build();
			} else if (waitingMillis > limitMillis) {
				healthMeasure = HealthMeasure.builder()
						.withYellowStatus("Daemon " + daemonStat.getDaemonName() + " waiting for a free thread for " + waitingMillis / 1000 + "s (limit " + limitMillis / 1000 + "s)")
						.build();
			}
		}
		return healthMeasure;
	}

}

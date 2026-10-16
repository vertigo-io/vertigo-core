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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.health.HealthChecked;
import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.analytics.health.HealthMeasureBuilder;
import io.vertigo.core.daemon.DaemonManager;
import io.vertigo.core.daemon.DaemonPoolStat;
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

	private static final Logger LOG = LogManager.getLogger(DaemonManagerImpl.class);
	/** Multiplier of the daemon period in the dead-detection threshold (min 4 x period). */
	private static final int STUCK_PERIOD_MULTIPLIER = 4;
	/** Minimum duration (120s) a running daemon may take before being declared dead. */
	private static final long STUCK_DEAD_AFTER_MIN_MILLIS = 120_000L;
	/** Cap of the dead-detection threshold beyond the period (2h, bounds the detection delay for large periods). */
	private static final long STUCK_DEAD_AFTER_MAX_MILLIS = 2 * 3_600_000L;
	/** Interval between two starved-daemon WARN logs (rate limit). */
	private static final long STARVED_WARN_INTERVAL_MILLIS = 5 * 60_000L;

	private final DaemonExecutor daemonExecutor;
	private final Optional<Integer> daemonDeadAfterInSecond;
	private volatile boolean active;
	private final AtomicLong starvedLastWarnAt = new AtomicLong(0L);

	/**
	 * Construct an instance of DaemonManagerImpl.
	 *
	 * @param analyticsManager AnalyticsManager
	 * @param threadPoolSize thread pool size (optional, default 2)
	 * @param daemonDeadAfterInSecond minimum duration in seconds a running daemon may take before being declared dead (optional, default 120s)
	 */
	@Inject
	public DaemonManagerImpl(final AnalyticsManager analyticsManager,
			@ParamValue("threadPoolSize") final Optional<Integer> threadPoolSize,
			@ParamValue("daemonDeadAfterInSecond") final Optional<Integer> daemonDeadAfterInSecond) {
		Assertion.check().isNotNull(analyticsManager);
		//---
		daemonExecutor = new DaemonExecutor(analyticsManager, threadPoolSize.orElse(2));
		this.daemonDeadAfterInSecond = daemonDeadAfterInSecond;
		//--
		Node.getNode().registerPreActivateFunction(this::startAllDaemons);
	}

	/**
	 * Dead-detection threshold for a daemon: max(deadAfter, min(4 x period, period + 2h)).
	 * deadAfter is the MINIMUM duration a running daemon may take before being declared dead (default 120s):
	 * it protects frequent daemons (e.g. 1s period) from being declared dead on a short freeze.
	 * As a floor, an explicitly configured deadAfter is always respected, even beyond the cap.
	 * The cap period + 2h bounds the detection delay for large periods (a daily daemon is declared dead
	 * after 26h, not after 4 x 24h) and allows a run to legitimately take a bit more than one period.
	 *
	 * @param periodInSeconds daemon configured period
	 * @param deadAfterMillis minimum duration before a running daemon is declared dead, in millis
	 * @return dead-detection threshold in millis
	 */
	static long stuckThresholdMillis(final int periodInSeconds, final long deadAfterMillis) {
		final long periodMillis = periodInSeconds * 1000L;
		return Math.max(deadAfterMillis, Math.min(periodMillis * STUCK_PERIOD_MULTIPLIER, periodMillis + STUCK_DEAD_AFTER_MAX_MILLIS));
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
		return daemonExecutor.getStats();
	}

	/** {@inheritDoc} */
	@Override
	public DaemonPoolStat getPoolStat() {
		return daemonExecutor.getPoolStat();
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

	// "All daemons failed" is intentionally RED (not capped at YELLOW): a total daemon failure is treated as a
	// systemic problem for which a pod restart is warranted. Accepted risk: a shared dependency down then
	// crashloops the pods. A daemon failing on its own (e.g. a dead external service) stays YELLOW.

	@HealthChecked(name = "lastExecs", feature = "daemons")
	public HealthMeasure checkDaemonsExecs() {
		final List<DaemonStat> daemonStats = getStats();
		final long failureCount = daemonStats.stream()
				.filter(daemonStat -> daemonStat.getCount() > 0) // to have a real indicator we use only daemon that have been executed at least once
				.filter(daemonStat -> !daemonStat.isLastExecSuccess())
				.count();
		//---
		final HealthMeasureBuilder healthMeasure = HealthMeasure.builder();
		if (failureCount == 0) {
			return healthMeasure
					.withGreenStatus()
					.build();
		} else if (failureCount < daemonStats.size()) {
			return healthMeasure
					.withYellowStatus("At least one daemon failed")
					.build();
		}
		return healthMeasure
				.withRedStatus("All daemons failed")
				.build();

	}

	// Liveness design rule: daemonsAlive is RED only when restarting the process is the only remedy that can help
	// (a daemon whose periodic schedule was cancelled by a fatal Error, a stuck daemon, a shut-down pool).
	// Load and saturation (a daemon waiting for a free thread) are NOT liveness signals - a restart cannot fix a
	// saturated pool (k8s would crashloop the pod): they are reported through getPoolStat()/DaemonStat (overdue,
	// lateness, last-end, consecutive failures) and WARN logs, never through a health status.
	// Known limits: durations are wall-clock (no nanoTime), so a forward clock step (NTP, VM restore) can give a
	// transient false RED and a backward step delays detection - accepted. A daemon calling external services
	// must set timeouts below the stuck threshold, or a hung call looks like a stuck daemon. A daemon run is
	// expected to take less than its period (a longer run is WARNed on each execution); 4 x period is the margin.
	// (A previous "poolUtilization" check based on the ScheduledThreadPoolExecutor queue size gave permanent false
	// positives - the queue holds every scheduled execution, not only waiting ones - and was removed.)

	@HealthChecked(name = "daemonsAlive", feature = "daemons")
	public HealthMeasure checkDaemonsAlive() {
		// defensive guard (the executor is package-private and only stopped by the manager's own stop()):
		// read isShutdown() before active - stop() clears active (volatile) before shutting the pool, so a check
		// that sees the pool shut down is guaranteed to read active == false on a graceful stop
		if (daemonExecutor.isShutdown() && active) {
			return HealthMeasure.builder()
					.withRedStatus("Daemon pool is shut down, no daemon can run anymore")
					.build();
		}
		//---
		if (active) {
			final List<String> deadDaemonNames = daemonExecutor.getDeadDaemonNames();
			if (!deadDaemonNames.isEmpty()) {
				LOG.error("Daemon(s) {} are dead: their periodic schedule was cancelled by a fatal Error, no future execution will occur", deadDaemonNames);
				return HealthMeasure.builder()
						.withRedStatus("Daemon(s) " + String.join(", ", deadDaemonNames)
								+ " are dead: their periodic schedule was cancelled by a fatal Error, no future execution will occur")
						.build();
			}
		}
		//---
		final long nowMillis = System.currentTimeMillis();
		final long deadAfterMillis = daemonDeadAfterInSecond
				.map(seconds -> seconds * 1000L)
				.orElse(STUCK_DEAD_AFTER_MIN_MILLIS);
		//---
		for (final DaemonStat daemonStat : daemonExecutor.getStats()) {
			if (daemonStat.getStatus() != DaemonStat.Status.running || daemonStat.getStartMillis() == 0) {
				continue;
			}
			//---
			final long runningMillis = nowMillis - daemonStat.getStartMillis();
			if (runningMillis > stuckThresholdMillis(daemonStat.getDaemonPeriodInSecond(), deadAfterMillis)) {
				final long runningSeconds = runningMillis / 1000;
				LOG.error("Daemon [{}] is stuck: running for {}s (period {}s), no longer executing",
						daemonStat.getDaemonName(), runningSeconds, daemonStat.getDaemonPeriodInSecond());
				return HealthMeasure.builder()
						.withRedStatus("Daemon " + daemonStat.getDaemonName() + " is stuck: running for " + runningSeconds
								+ "s (period " + daemonStat.getDaemonPeriodInSecond() + "s), no longer executing")
						.build();
			}
		}
		//---
		warnIfStarved();
		return HealthMeasure.builder().withGreenStatus().build();
	}

	/**
	 * Logs (rate-limited) when a daemon is starved: its next execution is overdue beyond max(period, 60s).
	 * Load is never a health status, only a signal for operators and services.
	 */
	private void warnIfStarved() {
		final DaemonExecutor.StarvedDaemon starved = daemonExecutor.getWorstStarvedDaemon();
		if (starved == null) {
			return;
		}
		//---
		final long nowMillis = System.currentTimeMillis();
		final long lastWarnAt = starvedLastWarnAt.get();
		if (nowMillis - lastWarnAt < STARVED_WARN_INTERVAL_MILLIS || !starvedLastWarnAt.compareAndSet(lastWarnAt, nowMillis)) {
			return;
		}
		//---
		final DaemonPoolStat poolStat = daemonExecutor.getPoolStat();
		LOG.warn("Daemon [{}] is starved: next execution overdue by {}s (pool: {} active / {} threads, {} overdue)",
				starved.name(), starved.overdueMillis() / 1000, poolStat.activeThreads(), poolStat.corePoolSize(), poolStat.overdueTasks());
	}

}

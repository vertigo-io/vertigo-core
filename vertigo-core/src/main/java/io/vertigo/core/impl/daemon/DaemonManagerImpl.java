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
	/** Maximum duration allowed beyond the period before a daemon is declared dead (2h, covers clock changes). */
	private static final long STUCK_DEAD_AFTER_MAX_MILLIS = 2 * 3_600_000L;

	private final DaemonExecutor daemonExecutor;
	private final Optional<Integer> daemonDeadAfterInSecond;

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
	 * Dead-detection threshold for a daemon: min(max(4 x period, deadAfter), period + 2h).
	 * deadAfter is the MINIMUM duration a running daemon may take before being declared dead (default 120s):
	 * it protects frequent daemons (e.g. 1s period) from being declared dead on a short freeze.
	 * For large periods the cap period + 2h applies instead (e.g. a daily daemon is declared dead after 26h,
	 * which covers the 25h long day of a summer-to-winter clock change).
	 *
	 * @param periodInSeconds daemon configured period
	 * @param deadAfterMillis minimum duration before a running daemon is declared dead, in millis
	 * @return dead-detection threshold in millis
	 */
	static long stuckThresholdMillis(final int periodInSeconds, final long deadAfterMillis) {
		final long periodMillis = periodInSeconds * 1000L;
		return Math.min(Math.max(periodMillis * STUCK_PERIOD_MULTIPLIER, deadAfterMillis), periodMillis + STUCK_DEAD_AFTER_MAX_MILLIS);
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
		daemonExecutor.start();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
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

	// No pool-saturation health check on purpose: a previous "poolUtilization" check (RED/YELLOW based on
	// ScheduledThreadPoolExecutor queue size) gave permanent false positives because the queue holds every
	// scheduled task, not only waiting ones. If pool saturation monitoring is ever needed, it belongs to a
	// metric or JMX, not to a liveness probe (k8s kills the pod on RED).

	@HealthChecked(name = "daemonsAlive", feature = "daemons")
	public HealthMeasure checkDaemonsAlive() {
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
		return HealthMeasure.builder().withGreenStatus().build();
	}

}

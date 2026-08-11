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

	private final DaemonExecutor daemonExecutor;

	/**
	 * Construct an instance of DaemonManagerImpl.
	 *
	 * @param analyticsManager AnalyticsManager
	 * @param threadPoolSize thread pool size (optional, default 2)
	 */
	@Inject
	public DaemonManagerImpl(final AnalyticsManager analyticsManager,
			@ParamValue("threadPoolSize") final Optional<Integer> threadPoolSize) {
		Assertion.check().isNotNull(analyticsManager);
		//---
		daemonExecutor = new DaemonExecutor(analyticsManager, threadPoolSize.orElse(2));
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
		return daemonExecutor.getStats();
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

	@HealthChecked(name = "poolUtilization", feature = "daemons")
	public HealthMeasure checkDaemonPoolUtilization() {
		final Map<String, Integer> poolStats = daemonExecutor.getPoolStats();
		final int activeThreads = poolStats.get("activeThreads");
		final int queuedTasks = poolStats.get("queuedTasks");
		final int poolSize = poolStats.get("poolSize");
		//---
		if (queuedTasks > 0) {
			LOG.warn("Daemon pool saturated - active: {}, queued: {}, poolSize: {}. "
					+ "Consider increasing threadPoolSize in boot.params or optimizing long-running daemons.",
					activeThreads, queuedTasks, poolSize);
		}
		//---
		final HealthMeasureBuilder healthMeasure = HealthMeasure.builder();
		if (queuedTasks == 0) {
			return healthMeasure.withGreenStatus().build();
		} else if (queuedTasks < poolSize) {
			return healthMeasure.withYellowStatus("Daemon pool has queued tasks: " + queuedTasks).build();
		}
		return healthMeasure.withRedStatus("Daemon pool critically saturated: " + queuedTasks + " queued").build();
	}

}

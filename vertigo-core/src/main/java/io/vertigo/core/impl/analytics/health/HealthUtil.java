/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
/**
 *
 */
package io.vertigo.core.impl.analytics.health;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.health.HealthChecked;
import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.analytics.health.HealthStatus;
import io.vertigo.core.analytics.health.definitions.HealthCheckDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.AspectPlugin;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.StringUtil;

/**
 * HealthAnalyticsUtil.
 *
 * @author jmforhan, mlaroche
 */
public final class HealthUtil {

	private static final String PLUGIN_COUNTER_CHAR = "#";// char used in plugins for counting them plugin#1, plugin#2

	private HealthUtil() {
		//private constructor for util classes
	}

	/**
	 * Registers all methods annotated with @Suscriber on the object
	 * @param componentId componentId to check
	 * @param component Component to check
	 * @param aopPlugin Aop plugin use for unwrap
	 * @return List of HealthCheckDefinition
	 */
	public static List<HealthCheckDefinition> createHealthCheckDefinitions(final String componentId, final CoreComponent component, final AspectPlugin aspectPlugin) {
		Assertion.check().isNotNull(component);

		//-- we construct a map of feature by componentId
		final Map<String, String> featureByComponentId = new HashMap<>();
		Node.getNode().getNodeConfig().bootConfig().coreComponentConfigs()
				.forEach(componentConfig -> featureByComponentId.put(componentConfig.getId(), "vertigo-boot"));

		Node.getNode().getNodeConfig().moduleConfigs()
				.forEach(moduleConfig -> moduleConfig.getComponentConfigs()
						.forEach(componentConfig -> featureByComponentId.put(componentConfig.getId(), moduleConfig.name())));
		//-----
		//1. search all methods
		return Stream.of(aspectPlugin.unwrap(component).getClass().getMethods())
				.filter(method -> method.isAnnotationPresent(HealthChecked.class))
				.map(method -> {
					final HealthChecked healthChecked = method.getAnnotation(HealthChecked.class);
					Assertion.check()
							.isTrue(HealthMeasure.class.equals(method.getReturnType()), "health check methods of class {0} must return a HealthMeasure instead of {1}", component.getClass(), method.getReturnType())
							.isTrue(method.getName().startsWith("check"), "health check methods of class {0} must start with check", component.getClass())
							.isTrue(method.getParameterTypes().length == 0, "health check methods of class {0} must not have any parameter", component.getClass());
					//-----
					//2. For each method register a listener
					// we remove # because it doesn't comply with definition naming rule
					final String healthCheckDefinitionName = "Hchk" + StringUtil.first2UpperCase(componentId.replace(PLUGIN_COUNTER_CHAR, "")) + "$" + method.getName();
					return new HealthCheckDefinition(
							healthCheckDefinitionName,
							healthChecked.name(),
							componentId,
							featureByComponentId.get(componentId),
							healthChecked.feature(),
							() -> (HealthMeasure) ClassUtil.invoke(component, method));
				})
				.toList();

	}

	public static List<HealthCheck> getHealthChecks() {
		return Node.getNode().getDefinitionSpace().getAll(HealthCheckDefinition.class).stream()
				.map(HealthUtil::buildHealthCheck)
				.toList();
	}

	private static HealthCheck buildHealthCheck(final HealthCheckDefinition healthCheckDefinition) {
		HealthMeasure healthMeasure;
		try {
			healthMeasure = healthCheckDefinition.getCheckMethod().get();
		} catch (final Exception e) {
			healthMeasure = HealthMeasure.builder()
					.withRedStatus(e.getMessage())
					.build();
		}
		return new HealthCheck(
				healthCheckDefinition.getHealthCheckName(),
				healthCheckDefinition.getChecker(),
				healthCheckDefinition.getModule(),
				healthCheckDefinition.getFeature(),
				Instant.now(),
				healthMeasure);
	}

	public static HealthStatus aggregate(final List<HealthCheck> healthChecks) {
		Assertion.check().isNotNull(healthChecks);
		//---
		int nbGreen = 0;
		int nbYellow = 0;
		int nbRed = 0;
		for (final HealthCheck healthCheck : healthChecks) {
			switch (healthCheck.healthMeasure().status()) {
				case GREEN -> nbGreen++;
				case YELLOW -> nbYellow++;
				case RED -> nbRed++;
			}
		}
		return generateStatus(nbGreen, nbYellow, nbRed);
	}

	private static HealthStatus generateStatus(
			final int nbGreen,
			final int nbYellow,
			final int nbRed) {
		if (nbRed > 0) {
			return HealthStatus.RED;
		}
		if (nbYellow > 0) {
			return HealthStatus.YELLOW;
		}
		if (nbGreen > 0) {
			return HealthStatus.GREEN;
		}
		return HealthStatus.GREEN; //by default
	}
}

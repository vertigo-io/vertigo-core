/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.commons.impl.health;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.app.Home;
import io.vertigo.commons.health.HealthCheck;
import io.vertigo.commons.health.HealthCheckDefinition;
import io.vertigo.commons.health.HealthChecked;
import io.vertigo.commons.health.HealthManager;
import io.vertigo.commons.health.HealthMeasure;
import io.vertigo.commons.health.HealthStatus;
import io.vertigo.core.component.AopPlugin;
import io.vertigo.core.component.Component;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

/**
 * HealthManager.
 *
 * @author jmforhan
 */
public final class HealthManagerImpl implements HealthManager, SimpleDefinitionProvider {

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		// here all

		// we need to unwrap the component to scan the real class and not the enhanced version
		final AopPlugin aopPlugin = Home.getApp().getConfig().getBootConfig().getAopPlugin();
		return Home.getApp().getComponentSpace().keySet()
				.stream()
				.flatMap(id -> createHealthCheckDefinitions(id, Home.getApp().getComponentSpace().resolve(id, Component.class), aopPlugin).stream())
				.collect(Collectors.toList());
	}

	/**
	 * Registers all methods annotated with @Suscriber on the object
	 * @param suscriberInstance
	 */
	private static List<HealthCheckDefinition> createHealthCheckDefinitions(final String componentId, final Component component, final AopPlugin aopPlugin) {
		Assertion.checkNotNull(component);

		//-- we construct a map of feature by componentId
		final Map<String, String> featureByComponentId = new HashMap<>();
		Home.getApp().getConfig().getModuleConfigs()
				.forEach(moduleConfig -> moduleConfig.getComponentConfigs()
						.forEach(componentConfig -> featureByComponentId.put(componentConfig.getId(), moduleConfig.getName())));
		//-----
		//1. search all methods
		return Stream.of(aopPlugin.unwrap(component).getClass().getMethods())
				.filter(method -> method.isAnnotationPresent(HealthChecked.class))
				.map(method -> {
					final HealthChecked healthChecked = method.getAnnotation(HealthChecked.class);
					Assertion.checkArgument(HealthMeasure.class.equals(method.getReturnType()), "health check methods of class {0} must return a HealthMeasure instead of {1}", component.getClass(), method.getReturnType());
					Assertion.checkArgument(method.getName().startsWith("check"), "health check methods of class {0} must start with check", component.getClass());
					Assertion.checkArgument(method.getParameterTypes().length == 0, "health check methods of class {0} must not have any parameter", component.getClass());
					//-----
					//2. For each method register a listener
					// we remove # because it doesn't comply with definition naming rule
					final String healthCheckDefinitionName = "HCHK_" + StringUtil.camelToConstCase(componentId.replaceAll("#", "")) + "$" + StringUtil.camelToConstCase(method.getName());
					return new HealthCheckDefinition(
							healthCheckDefinitionName,
							healthChecked.name(),
							componentId,
							featureByComponentId.get(componentId),
							healthChecked.topic(),
							() -> (HealthMeasure) ClassUtil.invoke(component, method));
				})
				.collect(Collectors.toList());

	}

	/** {@inheritDoc} */
	@Override
	public List<HealthCheck> getHealthChecks() {
		return Home.getApp().getDefinitionSpace().getAll(HealthCheckDefinition.class).stream()
				.map(healthCheckDefinition -> buildHealthCheck(healthCheckDefinition))
				.collect(Collectors.toList());
	}

	private static HealthCheck buildHealthCheck(final HealthCheckDefinition healthCheckDefinition) {
		try {
			final HealthMeasure healthMeasure = healthCheckDefinition.getCheckMethod().get();
			return new HealthCheck(
					healthCheckDefinition.getHealthCheckName(),
					healthCheckDefinition.getChecker(),
					healthCheckDefinition.getFeature(),
					healthCheckDefinition.getTopic(),
					Instant.now(),
					healthMeasure);
		} catch (final Exception e) {
			final HealthMeasure healthMeasure = HealthMeasure.builder()
					.withRedStatus("Impossible to get status", e)
					.build();
			return new HealthCheck(
					healthCheckDefinition.getHealthCheckName(),
					healthCheckDefinition.getChecker(),
					healthCheckDefinition.getFeature(),
					healthCheckDefinition.getTopic(),
					Instant.now(),
					healthMeasure);
		}
	}

	@Override
	public HealthStatus aggregate(final List<HealthCheck> healthChecks) {
		Assertion.checkNotNull(healthChecks);
		//---
		int nbGreen = 0;
		int nbYellow = 0;
		int nbRed = 0;
		for (final HealthCheck healthCheck : healthChecks) {
			switch (healthCheck.getMeasure().getStatus()) {
				case GREEN:
					nbGreen++;
					break;
				case YELLOW:
					nbYellow++;
					break;
				case RED:
					nbRed++;
					break;
				default:
					break;
			}
		}
		return generateStatus(nbGreen, nbYellow, nbRed);
	}

	private static HealthStatus generateStatus(
			final int nbGreen,
			final int nbYellow,
			final int nbRed) {
		if (nbRed == 0) {
			if (nbYellow == 0) {
				return HealthStatus.GREEN;
			}
			//yellow >0
			return HealthStatus.YELLOW;
		}
		//red >0
		if (nbYellow == 0 && nbGreen == 0) {
			return HealthStatus.RED;
		}
		//red>0
		return HealthStatus.YELLOW;
	}
}

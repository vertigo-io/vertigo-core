/**
 *
 */
package io.vertigo.commons.impl.health;

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.app.Home;
import io.vertigo.commons.health.HealthCheck;
import io.vertigo.commons.health.HealthCheckDefinition;
import io.vertigo.commons.health.HealthChecked;
import io.vertigo.commons.health.HealthManager;
import io.vertigo.commons.health.HealthMeasure;
import io.vertigo.commons.health.HealthStatus;
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
		return Home.getApp().getComponentSpace().keySet()
				.stream()
				.flatMap(id -> createHealthCheckDefinitions(id, Home.getApp().getComponentSpace().resolve(id, Component.class)).stream())
				.collect(Collectors.toList());
	}

	/**
	 * Registers all methods annotated with @Suscriber on the object
	 * @param suscriberInstance
	 */
	private static List<HealthCheckDefinition> createHealthCheckDefinitions(final String componentId, final Component component) {
		Assertion.checkNotNull(component);
		//-----
		//1. search all methods
		return ClassUtil.getAllMethods(component.getClass(), HealthChecked.class)
				.stream()
				.map(method -> {
					final HealthChecked healthChecked = method.getAnnotation(HealthChecked.class);
					Assertion.checkArgument(HealthMeasure.class.equals(method.getReturnType()), "health check methods of class {0} must return a HealthMeasure instead of {1}", component.getClass(), method.getReturnType());
					Assertion.checkArgument(method.getName().startsWith("check"), "health check methods of class {0} must start with check", component.getClass());
					Assertion.checkArgument(method.getParameterTypes().length == 0, "health check methods of class {0} must not have any parameter", component.getClass());
					//-----
					//2. For each method register a listener
					final String healthCheckDefinitionName = "HCHK_" + StringUtil.camelToConstCase(componentId) + "$" + StringUtil.camelToConstCase(method.getName());
					return new HealthCheckDefinition(healthCheckDefinitionName, healthChecked.name(), componentId, () -> (HealthMeasure) ClassUtil.invoke(component, method));
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
					healthMeasure);
		} catch (final Exception e) {
			final HealthMeasure healthMeasure = HealthMeasure.builder()
					.withRedStatus("Impossible to get status", e)
					.build();
			return new HealthCheck(
					healthCheckDefinition.getHealthCheckName(),
					healthCheckDefinition.getChecker(),
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

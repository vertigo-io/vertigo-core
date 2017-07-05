/**
 *
 */
package io.vertigo.commons.impl.health;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.app.Home;
import io.vertigo.commons.health.HealthComponentStatusSupplier;
import io.vertigo.commons.health.HealthControlPoint;
import io.vertigo.commons.health.HealthManager;
import io.vertigo.commons.health.HealthStatus;
import io.vertigo.core.component.Component;
import io.vertigo.core.component.ComponentSpace;
import io.vertigo.lang.Assertion;

/**
 * HealthManager.
 *
 * @author jmforhan
 */
public final class HealthManagerImpl implements HealthManager {
	/** {@inheritDoc} */
	@Override
	public List<HealthControlPoint> getControlPoints() {
		final List<HealthControlPoint> controlPoints = new ArrayList<>();
		try {
			final ComponentSpace componentSpace = Home.getApp().getComponentSpace();
			for (final String id : componentSpace.keySet()) {
				final Component component = componentSpace.resolve(id, Component.class);
				if (component instanceof HealthComponentStatusSupplier) {
					final HealthComponentStatusSupplier supplier = (HealthComponentStatusSupplier) component;
					for (final HealthControlPoint controlPoint : supplier.getControlPoints()) {
						controlPoints.add(controlPoint);
					}
				}
			}
		} catch (final Exception e) {
			final HealthControlPoint controlPoint = HealthControlPoint.of(this.getClass().getSimpleName())
					.withRedStatus("Impossible to get status", e)
					.build();
			controlPoints.add(controlPoint);
		}
		return controlPoints;
	}

	@Override
	public HealthStatus aggregate(final List<HealthControlPoint> controlPoints) {
		Assertion.checkNotNull(controlPoints);
		//---
		int nbGreen = 0;
		int nbYellow = 0;
		int nbRed = 0;
		for (final HealthControlPoint controlPoint : controlPoints) {
			switch (controlPoint.getStatus()) {
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

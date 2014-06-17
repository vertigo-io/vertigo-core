package io.vertigo.studio.plugins.reporting.domain.metrics.dependency;

import io.vertigo.studio.reporting.Metric;

/**
 * @author pchretien
 * Résultat du plugin JoinCount
 */
public final class DependencyMetric implements Metric {
	private final int count;

	/**
	 * Constructeur par défaut.
	 * @param count Nombre de reférence
	 */
	public DependencyMetric(final int count) {
		this.count = count;
	}

	/** {@inheritDoc} */
	public String getTitle() {
		return "Utilisation dans les dao";
	}

	/** {@inheritDoc} */
	public Integer getValue() {
		return count;
	}

	/** {@inheritDoc} */
	public String getValueInformation() {
		return null;
	}

	/** {@inheritDoc} */
	public String getUnit() {
		return "";
	}

	/** {@inheritDoc} */
	public Status getStatus() {
		return Status.Executed;
	}

}

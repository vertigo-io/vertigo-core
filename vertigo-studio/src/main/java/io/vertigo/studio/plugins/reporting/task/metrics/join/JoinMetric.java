package io.vertigo.studio.plugins.reporting.task.metrics.join;

import io.vertigo.studio.reporting.Metric;

/**
 * @author tchassagnette
 * @version $Id: JoinMetric.java,v 1.1 2013/07/11 10:04:05 npiedeloup Exp $
 * Résultat du plugin JoinCount
 */
public final class JoinMetric implements Metric {
	private final int joinCount;

	/**
	 * Constructeur par défaut.
	 */
	public JoinMetric(final int joinCount) {
		this.joinCount = joinCount;
	}

	public String getTitle() {
		return "Nombre de jointures";
	}

	public Integer getValue() {
		return joinCount;
	}

	/** {@inheritDoc} */
	public String getValueInformation() {
		return null;
	}

	public String getUnit() {
		return "";
	}

	public Status getStatus() {
		return Status.Executed;
	}

}

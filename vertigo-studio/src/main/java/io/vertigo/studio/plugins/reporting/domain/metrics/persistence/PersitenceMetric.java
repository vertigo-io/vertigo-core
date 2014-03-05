package io.vertigo.studio.plugins.reporting.domain.metrics.persistence;

import io.vertigo.studio.reporting.Metric;

/**
 * @author pchretien
 * @version $Id: PersitenceMetric.java,v 1.1 2013/07/11 10:04:04 npiedeloup Exp $
 * Résultat du plugin JoinCount
 */
public final class PersitenceMetric implements Metric {
	private final boolean persistent;
	private final Status status;

	/**
	 * Constructeur par défaut.
	 * @param persistent Si persistent
	 * @param test Si test Ok
	 */
	public PersitenceMetric(final boolean persistent, final boolean test) {
		this.persistent = persistent;
		if (test) {
			status = Status.Executed;
		} else {
			status = Status.Error;
		}
	}

	/** {@inheritDoc} */
	public String getTitle() {
		return "Persistance";
	}

	/** {@inheritDoc} */
	public Boolean getValue() {
		return persistent;
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
		return status;
	}

}

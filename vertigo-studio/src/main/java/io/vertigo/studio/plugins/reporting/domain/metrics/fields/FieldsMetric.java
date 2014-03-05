package io.vertigo.studio.plugins.reporting.domain.metrics.fields;

import io.vertigo.studio.reporting.Metric;

/**
 * @author pchretien
 * @version $Id: FieldsMetric.java,v 1.1 2013/07/11 10:04:05 npiedeloup Exp $
 * Résultat du plugin JoinCount
 */
public final class FieldsMetric implements Metric {
	private final int size;

	/**
	 * Constructeur par défaut.
	 * @param size Nombre de champs 
	 */
	public FieldsMetric(final int size) {
		this.size = size;
	}

	/** {@inheritDoc} */
	public String getTitle() {
		return "Nombre de champs";
	}

	/** {@inheritDoc} */
	public Integer getValue() {
		return size;
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

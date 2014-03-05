package io.vertigo.studio.plugins.reporting.task.metrics.requestsize;

import io.vertigo.studio.reporting.Metric;

/**
 * Composant d'affichage des résultats du plugin de calcul de la taille des requêtes.
 * 
 * @author tchassagnette
 * @version $Id: RequestSizeMetric.java,v 1.1 2013/07/11 10:04:05 npiedeloup Exp $
 */
public final class RequestSizeMetric implements Metric {
	private final int size;

	/**
	 * Constructeur par défaut.
	 */
	public RequestSizeMetric(final int size) {
		this.size = size;
	}

	public String getTitle() {
		return "Taille requête";
	}

	public Integer getValue() {
		return size;
	}

	/** {@inheritDoc} */
	public String getValueInformation() {
		return null;
	}

	public String getUnit() {
		return "caractères";
	}

	public Status getStatus() {
		return Status.Executed;
	}

}

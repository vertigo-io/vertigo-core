package io.vertigo.studio.plugins.reporting.domain.metrics.count;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.Metric;

/**
 * @author pchretien
 * @version $Id: CountMetric.java,v 1.8 2013/10/22 10:47:33 pchretien Exp $
 * Résultat du plugin JoinCount
 */
public final class CountMetric implements Metric {
	private final Integer count;
	private final Status status;

	/**
	 * Constructeur par défaut.
	 * @param count Nombre de lignes
	 * @param status Etat du test
	 */
	public CountMetric(final Integer count, final Status status) {
		Assertion.checkNotNull(status);
		switch (status) {
			case Rejected:
			case Error:
				Assertion.checkState(count == null, "count must be null ");
				break;
			case Executed:
				Assertion.checkNotNull(count);
				break;
			default:
				throw new IllegalArgumentException("case " + status + " not implemented");
		}
		//---------------------------------------------------------------------
		this.status = status;
		this.count = count;
	}

	/** {@inheritDoc} */
	public String getTitle() {
		return "Nbre lignes";
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
		return "rows";
	}

	/** {@inheritDoc} */
	public Status getStatus() {
		return status;
	}

}

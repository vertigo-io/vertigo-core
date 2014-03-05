package io.vertigo.studio.plugins.reporting.task.metrics.performance;

import io.vertigo.studio.reporting.Metric;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Composant d'afficahge des résultats du plugin de performance.
 *	
 * @author tchassagnette
 * @version $Id: PerformanceMetric.java,v 1.1 2013/07/11 10:04:04 npiedeloup Exp $
 */
public final class PerformanceMetric implements Metric {
	private final Long executionTime;
	private final Throwable throwable;
	private final Status status;

	/**
	 * Constructeur.
	 * @param executionTime Temps d'execution
	 */
	public PerformanceMetric(final long executionTime) {
		status = Status.Executed;
		this.executionTime = executionTime;
		throwable = null;
	}

	/**
	 * Constructeur en cas de rejet.
	 */
	public PerformanceMetric() {
		status = Status.Rejected;
		executionTime = null;
		throwable = null;
	}

	/**
	 * Constructeur en cas d'erreur.
	 * @param throwable erreur
	 */
	public PerformanceMetric(final Throwable throwable) {
		status = Status.Error;
		this.throwable = throwable;
		executionTime = null;
	}

	/** {@inheritDoc} */
	public String getTitle() {
		return "Temps d'exécution";
	}

	/** {@inheritDoc} */
	public Long getValue() {
		return executionTime;
	}

	/** {@inheritDoc} */
	public String getValueInformation() {
		if (status != Status.Error) {
			return null;
		}
		final StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		return sw.getBuffer().toString();
	}

	/** {@inheritDoc} */
	public String getUnit() {
		return "ms";
	}

	/** {@inheritDoc} */
	public Status getStatus() {
		return status;
	}
}

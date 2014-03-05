package io.vertigo.studio.plugins.reporting.task.metrics.explainplan;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.Metric;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Composant d'afficahge du résultat du Plugin d'exécution de l'explain plan.
 * 
 * @author tchassagnette
 * @version $Id: ExplainPlanMetric.java,v 1.3 2013/10/22 10:47:33 pchretien Exp $
 */
public final class ExplainPlanMetric implements Metric {
	private final String explainPlan;
	private final Status status;
	private final Throwable throwable;

	/**
	 * Constructeur.
	 * @param explainPlan Plan d'execution
	 */
	public ExplainPlanMetric(final String explainPlan) {
		Assertion.checkNotNull(explainPlan);
		//---------------------------------------------------------------------
		status = Status.Executed;
		this.explainPlan = explainPlan;
		throwable = null;
	}

	/**
	 * Constructeur.
	 */
	public ExplainPlanMetric() {
		//---------------------------------------------------------------------
		status = Status.Rejected;
		explainPlan = null;
		throwable = null;
	}

	/**
	 * Constructeur.
	 * @param throwable Exception
	 */
	public ExplainPlanMetric(final Throwable throwable) {
		Assertion.checkNotNull(throwable);
		//---------------------------------------------------------------------
		status = Status.Error;
		explainPlan = null;
		this.throwable = throwable;
	}

	/** {@inheritDoc} */
	public String getTitle() {
		return "Explain Plan";
	}

	/** {@inheritDoc} */
	public Object getValue() {
		if (explainPlan != null) {
			return explainPlan.split("TABLE ACCESS FULL").length - 1;
		}
		return null;
	}

	/** {@inheritDoc} */
	public String getValueInformation() {
		if (explainPlan != null) {
			return explainPlan.replaceAll("TABLE ACCESS FULL", "<b style='color: red;'>TABLE ACCESS FULL</b>");
		}
		if (status == Status.Error) {
			final StringWriter sw = new StringWriter();
			throwable.printStackTrace(new PrintWriter(sw));
			return sw.getBuffer().toString();
		}
		return "Rejected";
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

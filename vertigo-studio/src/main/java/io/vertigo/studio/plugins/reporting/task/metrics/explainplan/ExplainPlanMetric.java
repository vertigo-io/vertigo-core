/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.studio.plugins.reporting.task.metrics.explainplan;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.Metric;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Composant d'afficahge du résultat du Plugin d'exécution de l'explain plan.
 * 
 * @author tchassagnette
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

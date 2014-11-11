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
package io.vertigo.studio.plugins.reporting.domain;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.reporting.DataReport;
import io.vertigo.studio.reporting.Metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Résultat d'analyse d'une tâche.
 *
 * @author tchassagnette
 */
public final class DtDefinitionReport implements DataReport {
	private final DtDefinition dtDefinition;
	private final List<Metric> metrics;

	/**
	 * Constructeur par défaut.
	 */
	DtDefinitionReport(final DtDefinition dtDefinition, final List<Metric> metrics) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(metrics);
		//---------------------------------------------------------------------
		this.dtDefinition = dtDefinition;
		this.metrics = new ArrayList<>(metrics);
	}

	/** {@inheritDoc} */
	@Override
	public List<Metric> getMetrics() {
		return Collections.unmodifiableList(metrics);
	}

	@Override
	public String getTitle() {
		return dtDefinition.getLocalName();
	}

	@Override
	public String getFileName() {
		return dtDefinition.getLocalName() + ".html";
	}

	/** {@inheritDoc} */
	@Override
	public String getHtmlDescription() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<h1>").append(dtDefinition.getLocalName()).append("</h1>");
		sb.append("<h2>Propriétés</h2>");
		if (dtDefinition.isPersistent()) {
			sb.append("Persistant");
		} else {
			sb.append("non persistant>");
		}
		sb.append("<h2>Liste des champs</h2>");
		sb.append("<ul>");
		for (final DtField dtField : dtDefinition.getFields()) {
			sb
					.append("<li>")
					.append(dtField.getName())
					.append("<ul>")
					.append("<li>")
					.append("type:").append(dtField.getType().name())
					.append("</li><li>")
					.append("domain:").append(dtField.getDomain().getName())
					.append("</li><li>")
					.append("datatype:").append(dtField.getDomain().getDataType().name())
					.append("</li><li>")
					.append("persistant:").append(dtField.isPersistent())
					.append("</li><li>")
					.append("notNull:").append(dtField.isNotNull())
					.append("</li><li>")
					.append("label:").append(dtField.getLabel())
					.append("</li>")
					.append("</ul>")
					.append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
		//		sb.sb.append(dtDefinition.getTaskEngineClass().getCanonicalName());
		//		sb.sb.append("<h2>Requête</h2>");
		//		sb.sb.append(taskDefinition.getRequest());
		//		sb.sb.append("<h2>Exécution des plugins</h2>");
	}
}

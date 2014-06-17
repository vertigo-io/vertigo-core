package io.vertigo.studio.plugins.reporting.domain;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.kernel.lang.Assertion;
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
	public List<Metric> getMetrics() {
		return Collections.unmodifiableList(metrics);
	}

	public String getTitle() {
		return dtDefinition.getLocalName();
	}

	public String getFileName() {
		return dtDefinition.getLocalName() + ".html";
	}

	/** {@inheritDoc} */
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
			sb.append("<li>");
			sb.append(dtField.getName());
			sb.append("<ul>");
			sb.append("<li>");
			sb.append("type:").append(dtField.getType().name());
			sb.append("</li><li>");
			sb.append("domain:").append(dtField.getDomain().getName());
			sb.append("</li><li>");
			sb.append("datatype:").append(dtField.getDomain().getDataType().name());
			sb.append("</li><li>");
			sb.append("persistant:").append(dtField.isPersistent());
			sb.append("</li><li>");
			sb.append("notNull:").append(dtField.isNotNull());
			sb.append("</li><li>");
			sb.append("label:").append(dtField.getLabel());
			sb.append("</li>");
			sb.append("</ul>");
			sb.append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
		//		sb.sb.append(dtDefinition.getTaskEngineClass().getCanonicalName());
		//		sb.sb.append("<h2>Requête</h2>");
		//		sb.sb.append(taskDefinition.getRequest());
		//		sb.sb.append("<h2>Exécution des plugins</h2>");
	}
}

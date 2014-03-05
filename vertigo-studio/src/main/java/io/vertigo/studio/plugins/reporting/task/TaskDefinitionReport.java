package io.vertigo.studio.plugins.reporting.task;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
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
 * @version $Id: TaskDefinitionReport.java,v 1.6 2014/02/27 10:34:27 pchretien Exp $
 */
public final class TaskDefinitionReport implements DataReport {
	private final TaskDefinition taskDefinition;
	private final List<Metric> metrics;

	/**
	 * Constructeur par défaut.
	 * @param taskDefinition Définition de la tâche.
	 */
	TaskDefinitionReport(final TaskDefinition taskDefinition, final List<Metric> metrics) {
		Assertion.checkNotNull(taskDefinition);
		Assertion.checkNotNull(metrics);
		//---------------------------------------------------------------------
		this.taskDefinition = taskDefinition;
		this.metrics = new ArrayList<>(metrics);
	}

	/** {@inheritDoc} */
	public List<Metric> getMetrics() {
		return Collections.unmodifiableList(metrics);
	}

	/** {@inheritDoc} */
	public String getTitle() {
		return taskDefinition.getName();
	}

	/** {@inheritDoc} */
	public String getFileName() {
		return taskDefinition.getName() + ".html";
	}

	/** {@inheritDoc} */
	public String getHtmlDescription() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<h1>").append(taskDefinition.getName()).append("</h1>");
		sb.append("<h2>TaskEngine</h2>");
		sb.append(taskDefinition.getTaskEngineProvider().getName());
		sb.append("<h2>Requête</h2>");
		sb.append(taskDefinition.getRequest().replaceAll("\n", "<br/>"));
		sb.append("<h2>Exécution des plugins</h2>");
		return sb.toString();
	}
}

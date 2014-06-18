package io.vertigo.studio.plugins.mda.task;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Objet utilisé par FreeMarker.
 * 
 * @author pchretien
 */
public final class TemplatePAO {
	private final String packageName;
	private final String className;
	private final Collection<TemplateTaskDefinition> taskDefinitions;
	private final boolean hasOptions;

	/**
	 * Constructeur.
	 */
	TemplatePAO(final TaskConfiguration taskConfiguration, final Collection<TaskDefinition> taskDefinitionCollection, final String packageName) {
		Assertion.checkNotNull(taskConfiguration);
		Assertion.checkNotNull(taskDefinitionCollection);
		Assertion.checkArgument(!taskDefinitionCollection.isEmpty(), "Aucune tache dans le package {0}", packageName);
		Assertion.checkNotNull(packageName);
		// -----------------------------------------------------------------
		this.packageName = packageName;

		className = getLastPackagename(packageName) + "PAO";
		boolean hasOption = false;
		taskDefinitions = new ArrayList<>();
		for (final TaskDefinition taskDefinition : taskDefinitionCollection) {
			final TemplateTaskDefinition templateTaskDefinition = new TemplateTaskDefinition(taskDefinition);
			taskDefinitions.add(templateTaskDefinition);
			hasOption = hasOption || templateTaskDefinition.hasOptions();
		}
		hasOptions = hasOption;
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassSimpleName() {
		return className;
	}

	/**
	 * @return Liste des tasks
	 */
	public Collection<TemplateTaskDefinition> getTaskDefinitions() {
		return Collections.unmodifiableCollection(taskDefinitions);
	}

	/**
	 * @return Nom du package.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Retourne le nom du package feuille à partir d'un nom complet de package.
	 * exemple : org.company.sugar > sugar
	 * @param packageName Nom de package
	 * @return Nom du package feuille à partir d'un nom complet de package
	 */
	private static String getLastPackagename(final String packageName) {
		String lastPackageName = packageName;
		if (lastPackageName.indexOf('.') != -1) {
			lastPackageName = lastPackageName.substring(lastPackageName.lastIndexOf('.') + 1);
		}
		return StringUtil.first2UpperCase(lastPackageName);
	}

	/**
	 * @return Si ce pao utilise au moins une Option : vertigo.kernel.lang.Option
	 */
	public boolean isOptions() {
		return hasOptions;
	}
}

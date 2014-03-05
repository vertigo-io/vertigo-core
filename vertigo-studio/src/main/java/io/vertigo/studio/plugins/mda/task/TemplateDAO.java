package io.vertigo.studio.plugins.mda.task;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Objet utilisé par FreeMarker.
 * 
 * @author pchretien
 * @version $Id: TemplateDAO.java,v 1.7 2014/02/27 10:34:13 pchretien Exp $
 */
public final class TemplateDAO {
	private final DtDefinition dtDefinition;
	private final String packageName;
	private final Collection<TemplateTaskDefinition> taskDefinitions;
	private final boolean hasOptions;

	/**
	 * Constructeur.
	 * 
	 * @param dtDefinition DtDefinition de l'objet à générer
	 */
	TemplateDAO(final TaskConfiguration taskConfiguration, final DtDefinition dtDefinition, final Collection<TaskDefinition> taskDefinitionCollection) {
		Assertion.checkNotNull(taskConfiguration);
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(taskDefinitionCollection);
		final String definitionPackageName = dtDefinition.getPackageName();
		final String packageNamePrefix = taskConfiguration.getProjectPackageName() + ".domain";
		Assertion.checkArgument(definitionPackageName.contains(packageNamePrefix), "Le nom du package {0}, doit commencer par le prefix normalise: {1}", definitionPackageName, packageNamePrefix);
		// -----------------------------------------------------------------
		this.dtDefinition = dtDefinition;
		//On construit le nom du package à partir du package de la DT dans le quel on supprime le début.
		packageName = taskConfiguration.getDaoPackage() + definitionPackageName.substring(packageNamePrefix.length());

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
		return dtDefinition.getClassSimpleName() + "DAO";
	}

	/**
	 * @return Type de la PK
	 */
	public String getPkFieldType() {
		return dtDefinition.getIdField().get().getDomain().getDataType().getJavaClass().getCanonicalName();
	}

	/**
	 * @return Nom de la classe du Dt
	 */
	public String getDtClassCanonicalName() {
		return dtDefinition.getClassCanonicalName();
	}

	/**
	 * @return Nom du package
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Liste des tasks
	 */
	public Collection<TemplateTaskDefinition> getTaskDefinitions() {
		return taskDefinitions;
	}

	/**
	 * @return Si ce dao utilise au moins une option : vertigo.kernel.lang.Option
	 */
	public boolean isOptions() {
		return hasOptions;
	}

}

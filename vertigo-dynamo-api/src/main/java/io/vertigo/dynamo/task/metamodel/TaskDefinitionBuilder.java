package io.vertigo.dynamo.task.metamodel;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;

import java.util.ArrayList;
import java.util.List;

/** Builder des définitions de taches.
 *
 * @author  fconstantin, pchretien
 * @version $Id: TaskDefinitionBuilder.java,v 1.4 2014/01/20 17:45:43 pchretien Exp $
 */
public final class TaskDefinitionBuilder implements Builder<TaskDefinition> {
	private final List<TaskAttribute> taskAttributes = new ArrayList<>();

	private final String taskDefinitionName;
	private Class<? extends TaskEngine> taskEngineClass;
	private String request;
	private String packageName;

	/**
	 * Construction du builder.
	 *
	 * @param taskDefinitionName Nom de la définition de la tache
	 */
	public TaskDefinitionBuilder(final String taskDefinitionName) {
		Assertion.checkNotNull(taskDefinitionName);
		//----------------------------------------------------------------------
		this.taskDefinitionName = taskDefinitionName;
	}

	/**
	 * Initialise une définition de tache.
	 *
	 * @param newTaskEngineClass Classe réalisant l'implémentation
	 */
	public TaskDefinitionBuilder withEngine(final Class<? extends TaskEngine> newTaskEngineClass) {
		Assertion.checkNotNull(newTaskEngineClass);
		//Il est important de refaire le test car les test de cast ne sont pas fiable avec les generics
		if (newTaskEngineClass.isAssignableFrom(TaskEngine.class)) {
			throw new ClassCastException("La classe doit être une sous classe de ServiceProvider");
		}
		//---------------------------------------------------------------------
		taskEngineClass = newTaskEngineClass;
		return this;
	}

	/**
	 * @param newRequest Chaine de configuration de la tache
	 */
	public TaskDefinitionBuilder withRequest(final String newRequest) {
		Assertion.checkNotNull(newRequest);
		//---------------------------------------------------------------------
		//Pour unifier la saisie de la request sous un environnement unix ou dos
		// et pour éviter la disparité de gestion des retours chariot
		//par certains drivers de base de données.
		request = newRequest.replace("\r", "");
		return this;
	}

	/**
	 * @param newPackageName Nom du package
	 */
	public TaskDefinitionBuilder withPackageName(final String newPackageName) {
		//packageName peut être null
		//---------------------------------------------------------------------
		packageName = newPackageName;
		return this;
	}

	/**
	 * Ajoute un attribut à une définition de tache.
	 *
	 * @param attributeName Nom de l'attribut
	 * @param domain Domaine de l'attribut
	 * @param notNull Si attribut obligatoirement non null
	 * @param in Si attribut entrant
	 */
	public TaskDefinitionBuilder withAttribute(final String attributeName, final Domain domain, final boolean notNull, final boolean in) {
		Assertion.checkNotNull(attributeName);
		Assertion.checkNotNull(domain);
		//----------------------------------------------------------------------
		final TaskAttribute taskAttribute = new TaskAttribute(attributeName, domain, notNull, in);
		taskAttributes.add(taskAttribute);
		return this;
	}

	/** {@inheritDoc} */
	public TaskDefinition build() {
		return new TaskDefinition(taskDefinitionName, packageName, taskEngineClass, request, taskAttributes);
	}
}

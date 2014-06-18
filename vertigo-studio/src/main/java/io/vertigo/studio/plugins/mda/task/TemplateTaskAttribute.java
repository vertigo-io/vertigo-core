package io.vertigo.studio.plugins.mda.task;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.studio.plugins.mda.domain.DomainUtil;

/** 
 * Génération des classes/méthodes des taches de type DAO.
 * 
 * @author pchretien
 */
public final class TemplateTaskAttribute {
	private final TaskAttribute taskAttribute;
	private final TaskDefinition taskDefinition;

	TemplateTaskAttribute(final TaskDefinition taskDefinition, final TaskAttribute taskAttribute) {
		Assertion.checkNotNull(taskDefinition);
		Assertion.checkNotNull(taskAttribute);
		//-----------------------------------------------------------------
		this.taskAttribute = taskAttribute;
		this.taskDefinition = taskDefinition;
	}

	/**
	 * @return Nom de l'attribut.
	 */
	public String getName() {
		return taskAttribute.getName();
	}

	/**
	 * @return Nom de la constante
	 */
	public String getConstantName() {
		final String inOut = taskAttribute.isIn() ? "IN_" : "OUT_";
		return "ATTR_" + inOut + taskDefinition.getName() + '_' + taskAttribute.getName();
	}

	/**
	 * @return Nom de la variable
	 */
	public String getVariableName() {
		return StringUtil.constToCamelCase(taskAttribute.getName(), false);
	}

	/**
	 * @return Type de la donnée en string
	 */
	public String getDataType() {
		return String.valueOf(DomainUtil.buildJavaType(taskAttribute.getDomain()));
	}

	/**
	 * VRAI si l'attribut est entrant
	 * FAUX si l'attribut est créé par la tache donc sortant.
	 * @return Si l'attribut est entrant.
	 */
	public boolean isIn() {
		return taskAttribute.isIn();
	}

	/**
	 * @return Si l'attribut est obligatoire.
	 */
	public boolean isNotNull() {
		return taskAttribute.isNotNull();
	}

	/**
	 * @return Domain.
	 */
	Domain getDomain() {
		return taskAttribute.getDomain();
	}
}

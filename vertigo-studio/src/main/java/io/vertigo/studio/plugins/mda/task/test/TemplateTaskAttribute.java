package io.vertigo.studio.plugins.mda.task.test;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.util.DomainUtil;

/**
 * Représente un attribut de task.
 *
 * @author sezratty
 */
public final class TemplateTaskAttribute {
	private final TaskAttribute taskAttribute;
	private final TaskDefinition taskDefinition;
	private final DumExpression value;

	TemplateTaskAttribute(final TaskDefinition taskDefinition, final TaskAttribute taskAttribute) {
		Assertion.checkNotNull(taskDefinition);
		Assertion.checkNotNull(taskAttribute);
		//-----
		this.taskAttribute = taskAttribute;
		this.taskDefinition = taskDefinition;
		this.value = DumExpression.create(this.taskAttribute.getDomain(), this.taskAttribute.isRequired());
	}

	/**
	 * @return Nom de l'attribut.
	 */
	public String getName() {
		return taskAttribute.getName();
	}

	/**
	 * @return Type de la donnée en string
	 */
	public String getDataType() {
		return String.valueOf(DomainUtil.buildJavaType(taskAttribute.getDomain()));
	}
	
	/**
	 * @return L'expression de la valeur factice.
	 */
	public DumExpression getValue() {
		return value;
	}

	/**
	 * @return Domain.
	 */
	Domain getDomain() {
		return taskAttribute.getDomain();
	}
}

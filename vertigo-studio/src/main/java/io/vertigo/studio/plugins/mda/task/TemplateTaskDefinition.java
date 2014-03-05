package io.vertigo.studio.plugins.mda.task;

import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Génération des classes/méthodes des taches de type DAO.
 * 
 * @author pchretien
 * @version $Id: TemplateTaskDefinition.java,v 1.5 2014/02/27 10:38:47 pchretien Exp $
 */
public final class TemplateTaskDefinition {
	private final TaskDefinition taskDefinition;
	private final List<TemplateTaskAttribute> ins = new ArrayList<>();
	private final Collection<TemplateTaskAttribute> attributes = new ArrayList<>();

	private final TemplateTaskAttribute out;
	private final boolean hasOptions;

	TemplateTaskDefinition(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//-----------------------------------------------------------------
		this.taskDefinition = taskDefinition;
		TemplateTaskAttribute outTemp = null;
		boolean hasOption = false;
		for (final TaskAttribute attribute : taskDefinition.getAttributes()) {
			final TemplateTaskAttribute templateTaskAttribute = new TemplateTaskAttribute(taskDefinition, attribute);

			attributes.add(templateTaskAttribute);
			if (attribute.isIn()) {
				ins.add(templateTaskAttribute);
			} else {
				//On est dans le cas des paramètres OUT
				if (outTemp == null) {
					outTemp = templateTaskAttribute;
				} else {
					throw new Error("Les générations acceptent au plus un paramètre OUT");
				}
			}
			hasOption = hasOption || !attribute.isNotNull();
		}
		out = outTemp;
		hasOptions = hasOption;
	}

	/**
	 * @return Urn de la taskDefinition
	 */
	public String getUrn() {
		return taskDefinition.getName();
	}

	/**
	 * @return Nom de la méthode en CamelCase
	 */
	public String getMethodName() {
		final String localName = taskDefinition.getLocalName();
		return StringUtil.constToCamelCase(localName, false);
	}

	/**
	 * @return Liste des attributs
	 */
	public Collection<TemplateTaskAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * @return Liste des attributs en entréee
	 */
	public List<TemplateTaskAttribute> getInAttributes() {
		return ins;
	}

	/**
	 * @return Si la méthode possède un type de retour (sinon void)
	 */
	public boolean isOut() {
		return out != null;
	}

	/**
	 * @return Attribut de sortie (Unique)
	 */
	public TemplateTaskAttribute getOutAttribute() {
		Assertion.checkNotNull(out);
		//---------------------------------------------------------------------
		return out;
	}

	/**
	 * @return Si cette task utilise vertigo.kernel.lang.Option
	 */
	public boolean hasOptions() {
		return hasOptions;
	}
}

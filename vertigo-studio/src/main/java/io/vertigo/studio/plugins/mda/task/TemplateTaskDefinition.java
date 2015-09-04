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
package io.vertigo.studio.plugins.mda.task;

import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Génération des classes/méthodes des taches de type DAO.
 *
 * @author pchretien
 */
public final class TemplateTaskDefinition {
	private final TaskDefinition taskDefinition;
	private final List<TemplateTaskAttribute> ins = new ArrayList<>();
	private final Collection<TemplateTaskAttribute> attributes = new ArrayList<>();

	private final TemplateTaskAttribute out;
	private final boolean hasOptions;

	TemplateTaskDefinition(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//-----
		this.taskDefinition = taskDefinition;
		boolean hasOption = false;

		for (final TaskAttribute attribute : taskDefinition.getInAttributes()) {
			final TemplateTaskAttribute templateTaskAttribute = new TemplateTaskAttribute(taskDefinition, attribute);
			attributes.add(templateTaskAttribute);
			ins.add(templateTaskAttribute);
			hasOption = hasOption || !attribute.isNotNull();
		}

		if (taskDefinition.getOutAttributeOption().isDefined()) {
			final TaskAttribute attribute = taskDefinition.getOutAttributeOption().get();
			final TemplateTaskAttribute templateTaskAttribute = new TemplateTaskAttribute(taskDefinition, attribute);
			attributes.add(templateTaskAttribute);
			//On est dans le cas des paramètres OUT
			out = templateTaskAttribute;
			hasOption = hasOption || !attribute.isNotNull();
		} else {
			out = null;
		}
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
		return StringUtil.constToLowerCamelCase(localName);
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
		//-----
		return out;
	}

	/**
	 * @return Si cette task utilise vertigo.core.lang.Option
	 */
	public boolean hasOptions() {
		return hasOptions;
	}
}

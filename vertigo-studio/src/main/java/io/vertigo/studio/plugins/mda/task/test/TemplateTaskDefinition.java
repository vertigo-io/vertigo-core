/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.plugins.mda.task.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Génération des classes/méthodes des taches de type DAO.
 *
 * @author sezratty
 */
public final class TemplateTaskDefinition {
	private final TaskDefinition taskDefinition;
	private final List<TemplateTaskAttribute> ins = new ArrayList<>();
	private final TemplateTaskAttribute out;
	private final String testPackageName;
	private final String testClassName;
	private final String packageName;
	private final String className;
	private final Set<String> imports = new HashSet<>();

	private final boolean hasOptions;

	TemplateTaskDefinition(final TaskDefinition taskDefinition, final String packageName, final String className) {
		Assertion.checkNotNull(taskDefinition);
		Assertion.checkNotNull(packageName);
		Assertion.checkNotNull(className);
		//-----
		this.taskDefinition = taskDefinition;
		this.packageName = packageName;
		this.className = className;

		testPackageName = this.packageName + "." + StringUtil.first2LowerCase(this.className) + "Test";
		testClassName = StringUtil.first2UpperCase(getMethodName()) + "Test";

		imports.add(packageName + "." + getClassName());

		// Paramètres in
		boolean hasOption = false;
		for (final TaskAttribute attribute : taskDefinition.getInAttributes()) {
			final TemplateTaskAttribute templateTaskAttribute = new TemplateTaskAttribute(taskDefinition, attribute);
			ins.add(templateTaskAttribute);
			imports.addAll(templateTaskAttribute.getValue().getImports());

			hasOption = hasOption || !attribute.isRequired();
		}

		// Paramètre out
		final Optional<TaskAttribute> outAttributeOption = taskDefinition.getOutAttributeOption();
		out = outAttributeOption.isPresent() ? new TemplateTaskAttribute(taskDefinition, outAttributeOption.get()) : null;

		hasOptions = hasOption;
	}

	/**
	 * @return Nom de la méthode en CamelCase
	 */
	public String getMethodName() {
		final String localName = DefinitionUtil.getLocalName(taskDefinition.getName(), TaskDefinition.class);
		return StringUtil.constToLowerCamelCase(localName);
	}

	/**
	 * @return Nom du package de test en cascalCase
	 */
	public String getTestPackageName() {
		return testPackageName;
	}

	/**
	 * @return Nom simple de la classe de test en PascalCase
	 */
	public String getTestClassName() {
		return testClassName;
	}

	/**
	 * @return Nom canonique de la classe de test
	 */
	public String getTestClassCanonicalName() {
		return testPackageName + "." + testClassName;
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return Nom de la variable PAO dans le test.
	 */
	public String getDaoVariable() {
		return StringUtil.first2LowerCase(className);
	}

	/**
	 * @return Nom de la méthode de test en CamelCase
	 */
	public String getTestMethodName() {
		return "check_" + getMethodName() + "_Ok";
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

	/**
	 * @return Liste des imports.
	 */
	public List<String> getImports() {
		// TODO tri ?
		return new ArrayList<>(imports);
	}
}

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
package io.vertigo.quarto.publisher.impl.merger.script;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

/**
 * Stocke le contenu d'un tag de la grammaire ODT
 * en distiguant son type et un attribut.
 * @author oboitel
 * @version $Id: ScriptTagContent.java,v 1.5 2013/10/22 10:49:47 pchretien Exp $
 */
public final class ScriptTagContent {
	private final ScriptTagDefinition definition;
	private final String attribute;
	private String variableName;

	ScriptTagContent(final ScriptTagDefinition definition, final String attribute) {
		Assertion.checkNotNull(definition);
		Assertion.checkArgument(attribute == null || attribute.length() > 0, "Les attributs doivent faire plus de 1 caract�re");
		//---------------------------------------------------------------------
		this.definition = definition;
		this.attribute = attribute;
	}

	/**
	 * Si aucun attribut une exception est retourn�e.
	 * @return Atribut du tag
	 */
	public String getAttribute() {
		checkAttribute();
		return attribute;
	}

	/**
	 * @return Variable java courante
	 */
	public String getCurrentVariable() {
		return variableName;
	}

	/**
	 * @param variable Variable java courante
	 */
	void setCurrentVariable(final String variable) {
		variableName = variable;
	}

	/**
	 * @return Definition du tag
	 */
	ScriptTagDefinition getScriptTagDefinition() {
		return definition;
	}

	/**
	 * @return Si le tag a un attribut
	 */
	private boolean hasAttribute() {
		return attribute != null;
	}

	/**
	 * V�rifie que le Tag poss�de un attribut sinon lance une exception.
	 */
	private void checkAttribute() {
		if (!hasAttribute()) {
			throw new VRuntimeException("tag malform� : le tag {0} doit avoir un attribut", null, getScriptTagDefinition().getName());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "tag::" + definition.getName() + '[' + attribute + ']';
	}
}

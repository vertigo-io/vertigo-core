/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2021, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.definition;

import java.io.Serializable;
import java.util.regex.Pattern;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;

/**
 * A definition is identified by an id called DefinitionId
 *
 * This id is composed of
 *  - a prefix
 *  - a shortName
 *  - a link towards the Definition
 *
 * fullName = prefix + shortName
 *
 * @author  pchretien
 *
 * @param The fullName of the definition (must start with the prefix)
 * prefix The prefix
 */
public final class DefinitionId<D extends Definition> implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String fullName;
	private final String prefix;
	private transient D definition;

	/**
	 * A definition must have a unique fullName, which matches the following patterns :
	 * PrefixAaaaBbbb123
	 * or
	 * PrefixAaaaBbbb123$abcAbc123
	 */
	private static final Pattern REGEX_DEFINITION_NAME = Pattern.compile("[A-Z][a-zA-Z0-9]{2,60}([$][a-z][a-zA-Z0-9]{2,60})?");

	DefinitionId(final String fullName, final D definition) {
		this(fullName, definition.getClass());
	}

	DefinitionId(final String fullName, final Class<?> definitionClass) {
		final DefinitionPrefix definitionPrefix = definitionClass.getAnnotation(DefinitionPrefix.class);
		Assertion.check().isNotNull(definitionPrefix, "Annotation '@DefinitionPrefix' not found on {0}", definition.getClass().getName());
		this.prefix = definitionPrefix.value();

		Assertion.check()
				.isNotBlank(prefix)
				.isNotBlank(fullName)
				.isNotNull(definition)
				.isTrue(fullName.startsWith(prefix), "the definition id {0} must start with {1}", fullName, prefix)
				.isTrue(fullName.length() > prefix.length(), "the fullName of the definition is required")
				.isTrue(Character.isUpperCase(fullName.charAt(prefix.length())), "the name of the definition {0} must be in UpperCamelCase", fullName)
				.isTrue(REGEX_DEFINITION_NAME.matcher(fullName).matches(), "the definition name {0} must match this pattern {1}", fullName, REGEX_DEFINITION_NAME);
		//---
		this.fullName = fullName;
		this.definition = definition;
	}

	public String fullName() {
		return fullName;
	}

	public String prefix() {
		return prefix;
	}

	public String shortName() {
		return fullName().substring(prefix().length());
	}

	/**
	 * @return Objet référencé
	 */
	public synchronized D get() {
		if (definition == null) {
			definition = (D) Node.getNode().getDefinitionSpace().resolve(fullName, Definition.class);
		}
		return definition;

	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		return o instanceof DefinitionId<?>
				&& fullName.equals(DefinitionId.class.cast(o).fullName);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return fullName.hashCode();
	}
}

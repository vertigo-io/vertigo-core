package io.vertigo.core.node.definition;

import io.vertigo.core.lang.Assertion;

public abstract class AbstractDefinition implements Definition {
	private final String prefix;
	private final String name;

	protected AbstractDefinition(String name) {
		final DefinitionPrefix definitionPrefix = this.getClass().getAnnotation(DefinitionPrefix.class);
		prefix = definitionPrefix == null ? null : definitionPrefix.value();
		//---
		Assertion.check()
				.isNotNull(prefix, "Annotation '@DefinitionPrefix' not found on {0}", this.getClass().getName())
				.isNotBlank(prefix)
				.isNotBlank(name)
				.isTrue(name.startsWith(prefix), "La définition {0} doit commencer par {1}", name, prefix)
				.isTrue(name.length() > prefix.length(), "Le nom de la définition doit être renseigné")
				.isTrue(Character.isUpperCase(name.charAt(prefix.length())), "the name of the dtDefinition {0} must be in UpperCamelCase", name)
				.isTrue(REGEX_DEFINITION_NAME.matcher(name).matches(), "urn de définition {0} doit matcher le pattern {1}", name, Definition.REGEX_DEFINITION_NAME);
		//---
		this.name = name;
	}

	/** {@inheritDoc} */
	@Override
	public final String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public final String getLocalName() {
		return getName().substring(prefix.length());
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return name;
	}

}

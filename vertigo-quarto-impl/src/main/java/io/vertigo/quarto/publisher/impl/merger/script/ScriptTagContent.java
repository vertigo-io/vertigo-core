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

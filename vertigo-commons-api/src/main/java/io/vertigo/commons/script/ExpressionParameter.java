package io.vertigo.commons.script;

import io.vertigo.kernel.lang.Assertion;

/**
 * Paramètre.
 * Un paramètre est défini par 
 * - son nom
 * - sa valeur
 * - sont type java
 * 
 * @author  pchretien
 */
public final class ExpressionParameter {
	private final String name;
	private final Class<?> type;
	private final Object value;

	/**
	 * Constructeur definit un paramètre pour le ScriptEvaluator.
	 * @param name Nom du paramètre
	 * @param type Type du paramètre
	 * @param value Valeur du paramètre
	 */
	public ExpressionParameter(final String name, final Class<?> type, final Object value) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(type);
		Assertion.checkArgument(value == null || type.isInstance(value), "Valeur du paramètre '{0}' inconsistant avec son type '{1}'", name, type.getSimpleName());
		//---------------------------------------------------------------------
		this.name = name;
		this.type = type;
		this.value = value;
	}

	/**
	 * @return Nom du paramètre
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Type du paramètre
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * @return Valeur du paramètre
	 */
	public Object getValue() {
		return value;
	}
}

package io.vertigo.commons.script;

import io.vertigo.kernel.lang.Assertion;

/**
 * Param�tre.
 * Un param�tre est d�fini par 
 * - son nom
 * - sa valeur
 * - sont type java
 * 
 * @author  pchretien
 * @version $Id: ExpressionParameter.java,v 1.4 2013/10/22 12:31:36 pchretien Exp $
 */
public final class ExpressionParameter {
	private final String name;
	private final Class<?> type;
	private final Object value;

	/**
	 * Constructeur definit un param�tre pour le ScriptEvaluator.
	 * @param name Nom du param�tre
	 * @param type Type du param�tre
	 * @param value Valeur du param�tre
	 */
	public ExpressionParameter(final String name, final Class<?> type, final Object value) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(type);
		Assertion.checkArgument(value == null || type.isInstance(value), "Valeur du param�tre '{0}' inconsistant avec son type '{1}'", name, type.getSimpleName());
		//---------------------------------------------------------------------
		this.name = name;
		this.type = type;
		this.value = value;
	}

	/**
	 * @return Nom du param�tre
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Type du param�tre
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * @return Valeur du param�tre
	 */
	public Object getValue() {
		return value;
	}
}

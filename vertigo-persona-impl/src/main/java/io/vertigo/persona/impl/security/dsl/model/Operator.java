package io.vertigo.persona.impl.security.dsl.model;

/**
 * Operators.
 * @author npiedeloup
 */
public interface Operator {

	/**
	 * @return List of authorized string for this operator
	 */
	String[] authorizedString();
}

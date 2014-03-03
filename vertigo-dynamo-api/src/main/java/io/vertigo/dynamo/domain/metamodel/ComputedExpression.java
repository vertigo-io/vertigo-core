package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.kernel.lang.Assertion;

/**
 * Expression d'un champs Computed.
 * @author npiedeloup
 * @version $Id: ComputedExpression.java,v 1.4 2013/10/22 12:25:18 pchretien Exp $
 */
public final class ComputedExpression {
	private final String javaCode;

	/**
	 * Constructeur.
	 * @param javaCode Code java de l'expression
	 */
	public ComputedExpression(final String javaCode) {
		this.javaCode = javaCode;
	}

	/**
	 * @return Code java associée à cette expression
	 */
	public String getJavaCode() {
		Assertion.checkArgNotEmpty(javaCode, "Le code java de l'expression est obligatoire.");
		//---------------------------------------------------------------------
		return javaCode;
	}
}

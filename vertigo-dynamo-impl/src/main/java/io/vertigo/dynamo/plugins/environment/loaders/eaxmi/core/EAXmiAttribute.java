package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

import io.vertigo.kernel.lang.Assertion;

/**
* @author pforhan
*/
public final class EAXmiAttribute {
	private final String code;
	private final String label;
	private final boolean notNull;
	private final String domain;

	/**
	 * Constructeur.
	 */
	EAXmiAttribute(final String code, final String label, final boolean notNull, final String domain) {
		Assertion.checkArgNotEmpty(code);
		Assertion.checkArgNotEmpty(label);
		Assertion.checkArgNotEmpty(code);
		Assertion.checkArgNotEmpty(domain, "Le domain du champ '{0}' a �t� oubli�.", label);
		//----------------------------------------------------------------------
		this.code = code;
		this.label = label;
		this.notNull = notNull;
		this.domain = domain;
	}

	/**
	 * @return Code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return Libell�.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return Si l'attribut est persistent.
	 */
	public boolean isPersistent() {
		// L'information de persistence ne peut pas �tre d�duite du Xmi, tous les champs sont d�clar�s persistent de facto
		return true;
	}

	/**
	 * @return Si l'attribut est obligatoire.
	 */
	public boolean isNotNull() {
		return notNull;
	}

	/**
	 * @return Type de l'attribut.
	 */
	public String getDomain() {
		return domain;
	}

}

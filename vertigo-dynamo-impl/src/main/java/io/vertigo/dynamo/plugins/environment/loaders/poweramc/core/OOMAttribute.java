package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import io.vertigo.kernel.lang.Assertion;

/**
 * Implémentation de référence.
 * @author pchretien
 */
public final class OOMAttribute {
	private final String code;
	private final String label;
	private final boolean persistent;
	private final boolean notNull;
	private final String domain;

	/**
	 * Constructeur.
	 */
	OOMAttribute(final String code, final String label, final boolean persistent, final boolean notNull, final String domain) {
		Assertion.checkArgNotEmpty(code);
		Assertion.checkArgNotEmpty(label);
		Assertion.checkArgNotEmpty(code);
		Assertion.checkArgNotEmpty(domain, "Le domain du champ '{0}' a été oublié.", label);
		//----------------------------------------------------------------------
		this.code = code;
		this.label = label;
		this.persistent = persistent;
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
	 * @return Libellé.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return Si l'attribut est persistent.
	 */
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 * @return Si l'attribut est obligatoire.
	 */
	public boolean isNotNull() {
		return notNull;
	}

	/**
	 * @return Domain au sens poweramc (Nom).
	 */
	public String getDomain() {
		return domain;
	}
}

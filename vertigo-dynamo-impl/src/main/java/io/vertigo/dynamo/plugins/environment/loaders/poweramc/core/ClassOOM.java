package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe ou entité.
 * @author pchretien
 * @version $Id: ClassOOM.java,v 1.3 2013/10/22 12:30:19 pchretien Exp $
 */
public final class ClassOOM {
	private final String code;
	private final String packageName;
	private final List<AttributeOOM> keyAttributes;
	private final List<AttributeOOM> fieldAttributes;

	ClassOOM(final String code, final String packageName, final List<AttributeOOM> keyAttributes, final List<AttributeOOM> fieldAttributes) {
		Assertion.checkArgNotEmpty(code);
		//Assertion.notEmpty(packageName);
		Assertion.checkNotNull(keyAttributes);
		Assertion.checkNotNull(fieldAttributes);
		//---------------------------------------------------------------------
		this.code = code;
		this.packageName = packageName;
		this.keyAttributes = Collections.unmodifiableList(new ArrayList<>(keyAttributes));
		this.fieldAttributes = Collections.unmodifiableList(new ArrayList<>(fieldAttributes));
	}

	/**
	 * @return Code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return Nom du package.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Listes des champs identifiants (PK).
	 */
	public List<AttributeOOM> getKeyAttributes() {
		return keyAttributes;
	}

	/***
	 * @return Liste des champs non PK.
	 */
	public List<AttributeOOM> getFieldAttributes() {
		return fieldAttributes;
	}
}

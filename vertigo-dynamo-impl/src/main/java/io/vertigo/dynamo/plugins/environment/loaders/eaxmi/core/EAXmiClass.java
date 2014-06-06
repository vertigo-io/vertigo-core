package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* @author pforhan
*/
public final class EAXmiClass {
	private final String code;
	private final String packageName;
	private final List<EAXmiAttribute> keyAttributes;
	private final List<EAXmiAttribute> fieldAttributes;

	EAXmiClass(final String code, final String packageName, final List<EAXmiAttribute> keyAttributes, final List<EAXmiAttribute> fieldAttributes) {
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
	public List<EAXmiAttribute> getKeyAttributes() {
		return keyAttributes;
	}

	/***
	 * @return Liste des champs non PK.
	 */
	public List<EAXmiAttribute> getFieldAttributes() {
		return fieldAttributes;
	}

}

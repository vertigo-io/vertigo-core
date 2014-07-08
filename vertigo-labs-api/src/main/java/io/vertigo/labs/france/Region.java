package io.vertigo.labs.france;

import io.vertigo.kernel.lang.Assertion;

/**
 * @author pchretien
 */
public final class Region /*implements DtObject*/{
	private final String code;
	private final String label;

	public Region(String code, String label) {
		Assertion.checkArgNotEmpty(code);
		Assertion.checkArgNotEmpty(label);
		//--------------------------------------------------------------------
		this.code = code;
		this.label = label;

	}

	public String getLabel() {
		return label;
	}

	public String getCode() {
		return code;
	}
}

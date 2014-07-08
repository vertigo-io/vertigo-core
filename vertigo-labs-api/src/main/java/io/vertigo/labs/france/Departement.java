package io.vertigo.labs.france;

import io.vertigo.kernel.lang.Assertion;

/**
 * @author pchretien
 */
public final class Departement /*implements DtObject*/{
	private final String code;
	private final String label;
	private final Region region;

	public Departement(String code, String label, Region region) {
		Assertion.checkArgNotEmpty(code);
		Assertion.checkArgNotEmpty(label);
		Assertion.checkNotNull(region);
		//--------------------------------------------------------------------
		this.code = code;
		this.label = label;
		this.region = region;
	}

	public String getLabel() {
		return label;
	}

	public String getCode() {
		return code;
	}

	public Region getRegion() {
		return region;
	}
}

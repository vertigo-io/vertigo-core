package io.vertigo.vega.rest;

public enum Honorific {
	Mr("MR_"),
	Miss("MIS"),
	Mrs("MRS"),
	Ms("MS_"),
	Dr("DR_"),
	Cpt("CAP"),
	Cch("CCH"),

	Off("OFF"),
	Rev("REV"),
	Fth("FTH"),
	PhD("PHD"),
	Mst("MST");

	private final String code;

	private Honorific(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}

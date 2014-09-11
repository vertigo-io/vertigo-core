package io.vertigo.struts2.core;

/**
 * Mode du formulaire 
 * @author npiedeloup
 */
public enum FormMode {
	/**
	 * Mode lecture seul.
	 */
	readOnly("xhtml_read"),
	/**
	 * Mode edition.
	 */
	edit("xhtml"),
	/**
	 * Mode création.
	 */
	create("xhtml");

	private final String themeName;

	private FormMode(final String themeName) {
		this.themeName = themeName;
	}

	/**
	 * @return Nom du theme utilisé.
	 */
	public String getTheme() {
		return themeName;
	}
}

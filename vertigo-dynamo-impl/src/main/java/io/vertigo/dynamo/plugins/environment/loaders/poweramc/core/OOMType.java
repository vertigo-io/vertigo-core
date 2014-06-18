package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

/**
 * Type des objets powerAMC.
 * Les correspondances dynamo sont précisées ci-dessous.
 *
 * @author pchretien
 */
enum OOMType {
	/**
	 * Objet OOM décrivant un Package >>Package.
	 */
	Package("o:Package"),
	/**
	 * Objet OOM décrivant une Class >> DtDefinition.
	 */
	Class("o:Class"),
	/**
	 * Objet OOM décrivant un Domain >> Domain.
	 */
	Domain("o:Domain"),
	/**
	 * Objet OOM décrivant un Attibute d'un OOM >> DtField.
	 */
	Attribute("o:Attribute"),
	/**
	 * OOM décrivant un Identifier >> Assignation du caractère PK d'un DtField.
	 */
	Identifier("o:Identifier"),
	/**
	 * OOM décrivant une Association >> Association.
	 */
	Association("o:Association"),
	/**
	 * Référence sur un objet OOM.
	 */
	Shortcut("o:Shortcut");

	private final String code;

	private OOMType(final String code) {
		this.code = code;
	}

	private String getCode() {
		return code;
	}

	static OOMType getType(final String name) {
		final OOMType type;
		if (Domain.getCode().equals(name)) {
			type = Domain;
		} else if (Package.getCode().equals(name)) {
			type = Package;
		} else if (Class.getCode().equals(name)) {
			type = Class;
		} else if (Shortcut.getCode().equals(name)) {
			type = Shortcut;
		} else if (Attribute.getCode().equals(name)) {
			type = Attribute;
		} else if (Identifier.getCode().equals(name)) {
			type = Identifier;
		} else if (Association.getCode().equals(name)) {
			type = Association;
		} else {
			//rien trouvé
			type = null;
		}
		return type;
	}

	static boolean isNodeByRef(final String name) {
		boolean ok = false;
		ok = ok || Domain.getCode().equals(name);
		ok = ok || Attribute.getCode().equals(name);
		ok = ok || Class.getCode().equals(name);
		ok = ok || Shortcut.getCode().equals(name);
		ok = ok || Identifier.getCode().equals(name);
		return ok;
	}
}

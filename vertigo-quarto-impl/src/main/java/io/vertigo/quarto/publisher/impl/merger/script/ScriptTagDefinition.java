package io.vertigo.quarto.publisher.impl.merger.script;

/**
 * Definition d'un Tag KScript.
 * @author pchretien, npiedeloup
 * @version $Id: ScriptTagDefinition.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
final class ScriptTagDefinition {
	private final String name;
	private final Class<? extends ScriptTag> classTag;
	private final Boolean openTag;

	ScriptTagDefinition(final String name, final Class<? extends ScriptTag> classTag, final Boolean openTag) {
		this.name = name;
		this.classTag = classTag;
		this.openTag = openTag;
	}

	/**
	 * @return Classe du tag.
	 */
	Class<? extends ScriptTag> getClassTag() {
		return this.classTag;
	}

	/**
	 * @return Si balise ouvrante, false si balise fermante, null si pas de body
	 */
	Boolean isOpenTag() {
		return this.openTag;
	}

	/**
	 * @return Nom du tag
	 */
	String getName() {
		return this.name;
	}
}

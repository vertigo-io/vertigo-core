package io.vertigo.quarto.publisher.impl.merger.grammar;

import io.vertigo.quarto.publisher.impl.merger.script.ScriptContext;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTag;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTagContent;

/**
 * @author pchretien, npiedeloup
 * @version $Id: TagEncodedField.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
//public car instanci� dynamiquement
public final class TagEncodedField extends AbstractKScriptTag implements ScriptTag {
	public static final String ENCODER = "encoder";

	private static final String CALL = "=" + ENCODER + ".encode({0})";

	/** {@inheritDoc} */
	public String renderOpen(final ScriptTagContent tag, final ScriptContext context) {
		final String[] parsing = parseAttribute(tag.getAttribute(), FIELD_PATH_CALL);
		// le tag est dans le bon format
		parsing[0] = getCallForFieldPath(parsing[0], tag.getCurrentVariable());

		return getTagRepresentation(CALL, parsing);
	}

	/** {@inheritDoc} */
	public String renderClose(final ScriptTagContent tag, final ScriptContext context) {
		return "";
	}
}

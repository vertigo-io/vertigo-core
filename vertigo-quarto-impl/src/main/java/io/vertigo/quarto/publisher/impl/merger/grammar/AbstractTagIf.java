package io.vertigo.quarto.publisher.impl.merger.grammar;

import io.vertigo.quarto.publisher.impl.merger.script.ScriptContext;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTag;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTagContent;

/**
 * @author pchretien, npiedeloup
 * @version $Id: AbstractTagIf.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
abstract class AbstractTagIf extends AbstractKScriptTag implements ScriptTag {
	private static final String CALL_IF = "if({0}) \\{ ";
	private static final String CALL_IF_NOT = "if (!({0})) \\{ ";
	private final String call;
	private final boolean equalsCondition;

	AbstractTagIf(final boolean caseIf, final boolean equalsCondition) {
		call = caseIf ? CALL_IF : CALL_IF_NOT;
		this.equalsCondition = equalsCondition;
	}

	/** {@inheritDoc} */
	public final String renderOpen(final ScriptTagContent tag, final ScriptContext context) {
		final String[] parsing = parseAttribute(tag.getAttribute(), equalsCondition ? FIELD_PATH_CALL_EQUALS_CONDITION : FIELD_PATH_CALL);
		// le tag est dans le bon format
		if (equalsCondition) {
			parsing[0] = getCallForEqualsBooleanFieldPath(parsing[1], parsing[3], tag.getCurrentVariable());
		} else {
			parsing[0] = getCallForBooleanFieldPath(parsing[0], tag.getCurrentVariable());
		}
		return getTagRepresentation(call, parsing);
	}

	/** {@inheritDoc} */
	public final String renderClose(final ScriptTagContent content, final ScriptContext context) {
		return START_BLOC_JSP + '}' + END_BLOC_JSP;
	}
}

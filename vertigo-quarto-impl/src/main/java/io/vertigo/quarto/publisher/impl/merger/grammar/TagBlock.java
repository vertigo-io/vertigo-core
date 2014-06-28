package io.vertigo.quarto.publisher.impl.merger.grammar;

import io.vertigo.kernel.util.StringUtil;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptContext;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTag;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTagContent;

/**
 * @author pchretien, npiedeloup
 * @version $Id: TagBlock.java,v 1.2 2013/10/22 10:49:59 pchretien Exp $
 */
//public car instanci� dynamiquement
public final class TagBlock extends AbstractKScriptTag implements ScriptTag {
	/** {@inheritDoc} */
	public String renderOpen(final ScriptTagContent tag, final ScriptContext context) {
		return START_BLOC_JSP + decode(tag.getAttribute()) + END_BLOC_JSP;
	}

	/** {@inheritDoc} */
	public String renderClose(final ScriptTagContent tag, final ScriptContext context) {
		return START_BLOC_JSP + decode(tag.getAttribute()) + END_BLOC_JSP;
	}

	private String decode(final String s) {
		//On d�code les caract�res qui peuvent avoir du sens dans un block
		final StringBuilder decode = new StringBuilder(s);
		StringUtil.replace(decode, "&quot;", "\"");
		StringUtil.replace(decode, "&lt;", "<");
		StringUtil.replace(decode, "&gt;", ">");
		return decode.toString();
	}
}

package io.vertigo.quarto.publisher.impl.merger.script;


/**
 * Tag KScript.
 * @author pchretien, npiedeloup
 * @version $Id: ScriptTag.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
public interface ScriptTag {
	/**
	 * @param content tag �valu�
	 * @param context Context d'�valuation
	 * @return Contenu a substituer au tag
	 */
	String renderOpen(ScriptTagContent content, ScriptContext context);

	/**
	 * @param content tag �valu�
	 * @param context Context d'�valuation
	 * @return Contenu a substituer au tag
	 */
	String renderClose(ScriptTagContent content, ScriptContext context);
}

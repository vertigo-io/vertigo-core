package io.vertigo.commons.impl.script;

import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * Simule le m�canisme JSP sur un fichier texte quelconque.
 * Remplace les �l�ments compris entre les s�parateurs par une �valuation dynamique.
 * <% %>  : permet d'ins�rer des blocs java
 * <%= %> : permet d'ajouter des �l�ments au texte
 *
 * @author  pchretien
 * @version $Id: ScriptParserHandlerImpl.java,v 1.3 2013/10/22 12:26:59 pchretien Exp $
 */
final class ScriptParserHandlerImpl implements ScriptParserHandler {
	private static final String TEXT_KEY_PREFIX = "ZZYY";
	private static final String TEXT_KEY_SUFFIX = "YYZZ";

	private final ExpressionEvaluatorPlugin expressionEvaluatorPlugin;

	private final String originalScript;

	private final StringBuilder evaluatedScript = new StringBuilder();
	/**
	 * Si il y a au moins un param�tre alors la requ�te est dynamique.
	 */
	private boolean isDynamic; //implicite = false;

	private final List<String> textList = new ArrayList<String>();
	private final List<ExpressionParameter> parameters;

	/**
	 * Constructeur.
	 * @param parameters Map des param�tres
	 */
	ScriptParserHandlerImpl(final ExpressionEvaluatorPlugin expressionEvaluatorPlugin, final String originalScript, final List<ExpressionParameter> parameters) {
		Assertion.checkNotNull(expressionEvaluatorPlugin);
		Assertion.checkArgNotEmpty(originalScript);
		Assertion.checkNotNull(parameters);
		//----------------------------------------------------------------------
		this.expressionEvaluatorPlugin = expressionEvaluatorPlugin;
		this.originalScript = originalScript;
		this.parameters = parameters;
	}

	/** {@inheritDoc} */
	public void onText(final String text) {
		evaluatedScript.append("query.append(\"");
		appendEncodedText(evaluatedScript, text);
		evaluatedScript.append("\");");
	}

	/** {@inheritDoc} */
	public void onExpression(final String expression, final ScriptSeparator separator) {
		isDynamic = true; //Si on entre dans cette m�thode, on est forc�ment dynamique
		if (expression.charAt(0) == '=') {
			// cas <%=s START_ATTRIBUTE
			//On ajoute une �valuation de l'attribut
			evaluatedScript.append("query.append(String.valueOf(").append(expression.substring(1)).append("));");
		} else {
			//On ajoute le bloc java
			evaluatedScript.append(expression);
		}
	}

	String eval() {
		if (isDynamic) {
			//On positionne un try catch 
			evaluatedScript.insert(0, "try { StringBuilder query = new StringBuilder();");
			//---
			evaluatedScript.append(" return query.toString();");
			evaluatedScript.append("} catch (Exception e) { throw new RuntimeException(e);}");
			// ------------------------------------------------------------------
			return scriptEvaluate(evaluatedScript.toString());
		}
		return originalScript;

	}

	private void appendEncodedText(final StringBuilder sb, final String text) {
		final int key = textList.size();
		textList.add(text);
		sb.append(TEXT_KEY_PREFIX).append(key).append(TEXT_KEY_SUFFIX);
	}

	private String decodeText(final String text) {
		final StringBuilder retour = new StringBuilder();
		int textStart = 0;
		int textEnd = -1;
		while ((textEnd = text.indexOf(TEXT_KEY_PREFIX, textStart)) >= 0) {
			final int keyStart = textEnd + TEXT_KEY_PREFIX.length();
			final int keyEnd = text.indexOf(TEXT_KEY_SUFFIX, keyStart);
			final String key = text.substring(keyStart, keyEnd);

			retour.append(text.substring(textStart, textEnd));
			retour.append(textList.get(Integer.parseInt(key)));
			textStart = keyEnd + TEXT_KEY_SUFFIX.length();
		}
		// Dernier morceau
		retour.append(text.substring(textStart));

		return retour.toString();
	}

	private String scriptEvaluate(final String script) {
		final String res = expressionEvaluatorPlugin.evaluate(script, parameters, String.class);
		return decodeText(res);
	}
}

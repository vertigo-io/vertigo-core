package io.vertigo.commonsimpl.script;

import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.kernel.lang.Assertion;

import java.util.List;

/**
 * Evaluation d'un script
 * 
 * @author  pchretien
 * @version $Id: ScriptEvaluator.java,v 1.3 2013/10/22 12:26:59 pchretien Exp $
 */
final class ScriptEvaluator {
	private final SeparatorType separatorType;
	private final List<ExpressionParameter> parameters;
	private final ExpressionEvaluatorPlugin expressionEvaluatorPlugin;

	/**
	 * Constructeur.
	 * @param separatorType SeparatorType.CLASSIC ou SeparatorType.XML ou SeparatorType.XML_CODE
	 */
	ScriptEvaluator(final ExpressionEvaluatorPlugin expressionEvaluatorPlugin, final SeparatorType separatorType, final List<ExpressionParameter> parameters) {
		Assertion.checkNotNull(expressionEvaluatorPlugin);
		Assertion.checkNotNull(parameters);
		Assertion.checkNotNull(parameters);
		//----------------------------------------------------
		this.separatorType = separatorType;
		this.parameters = parameters;
		this.expressionEvaluatorPlugin = expressionEvaluatorPlugin;
	}

	/**
	 * Evaluation du script.
	 * @param script Script � �valuer
	 * @return Script �valu�
	 */
	String evaluate(final String script) {
		Assertion.checkNotNull(script);
		Assertion.checkNotNull(parameters);
		//---------------------------------------------------------------------
		final ScriptParserHandlerImpl scriptHandler = new ScriptParserHandlerImpl(expressionEvaluatorPlugin, script, parameters);

		final ScriptParser scriptParser = new ScriptParser(separatorType.getSeparators());
		scriptParser.parse(script, scriptHandler);
		return scriptHandler.eval();
	}

}

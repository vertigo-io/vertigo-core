package io.vertigo.commons.impl.script;

import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.kernel.lang.Assertion;

import java.util.List;

import javax.inject.Inject;

/** 
 * Gestion des manipulations sur des scripts.
 *
 * @author pchretien
 */
public final class ScriptManagerImpl implements ScriptManager {
	/**
	 * Plugin pour l'évaluation d'expression.
	 */
	private final ExpressionEvaluatorPlugin expressionEvaluatorPlugin;

	@Inject
	public ScriptManagerImpl(final ExpressionEvaluatorPlugin expressionEvaluatorPlugin) {
		Assertion.checkNotNull(expressionEvaluatorPlugin);
		//---------------------------------------------------------------------
		this.expressionEvaluatorPlugin = expressionEvaluatorPlugin;
	}

	/** {@inheritDoc} */
	public void parse(final String script, final ScriptParserHandler scriptHandler, final List<ScriptSeparator> separators) {
		new ScriptParser(separators).parse(script, scriptHandler);
	}

	/** {@inheritDoc} */
	public String evaluateScript(final String script, final SeparatorType separatorType, final List<ExpressionParameter> parameters) {
		return new ScriptEvaluator(expressionEvaluatorPlugin, separatorType, parameters).evaluate(script);
	}

	/** {@inheritDoc} */
	public <J> J evaluateExpression(final String expression, final List<ExpressionParameter> parameters, final Class<J> type) {
		return expressionEvaluatorPlugin.evaluate("return " + expression + ";", parameters, type);
	}

}

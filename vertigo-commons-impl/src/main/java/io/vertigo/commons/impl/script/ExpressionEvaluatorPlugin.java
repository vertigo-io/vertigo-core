package io.vertigo.commons.impl.script;

import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.kernel.component.Plugin;

import java.util.List;

/**
 * Evaluation d'une expression.
 * 
 * @author  pchretien
 */
public interface ExpressionEvaluatorPlugin extends Plugin {
	/**
	 * Evaluation d'une expression.
	 * @param expression Expression
	 * @param parameters Paramètres
	 * @param type Type retourné
	 * @return Résultat de l'expression après évaluation
	 */
	<J> J evaluate(final String expression, List<ExpressionParameter> parameters, Class<J> type);
}

package io.vertigo.commons.impl.script;

import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.kernel.component.Plugin;

import java.util.List;

/**
 * Evaluation d'une expression.
 * 
 * @author  pchretien
 * @version $Id: ExpressionEvaluatorPlugin.java,v 1.2 2013/10/22 12:26:59 pchretien Exp $
 */
public interface ExpressionEvaluatorPlugin extends Plugin {
	/**
	 * Evaluation d'une expression.
	 * @param expression Expression
	 * @param parameters Param�tres
	 * @param type Type retourn�
	 * @return R�sultat de l'expression apr�s �valuation
	 */
	<J> J evaluate(final String expression, List<ExpressionParameter> parameters, Class<J> type);
}

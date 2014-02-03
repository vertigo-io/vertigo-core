package io.vertigo.commons.plugins.script.janino;

import io.vertigo.commons.impl.script.ExpressionEvaluatorPlugin;
import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.kernel.exception.VRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.codehaus.janino.ScriptEvaluator;

/**
 * Evaluation d'une expression en se basant sur Janino.
 *
 * @author  pchretien
 * @version $Id: JaninoExpressionEvaluatorPlugin.java,v 1.3 2013/10/22 12:29:54 pchretien Exp $
 */
public final class JaninoExpressionEvaluatorPlugin implements ExpressionEvaluatorPlugin {
	/** {@inheritDoc} */
	public <J> J evaluate(final String expression, final List<ExpressionParameter> parameters, final Class<J> type) {
		// ---------Initialisation des types et noms de param�tre------------
		final int size = parameters.size();

		final String[] parameterNames = new String[size];
		final Class<?>[] parameterTypes = new Class[size];
		final Object[] parameterValues = new Object[size];

		int i = 0;
		for (final ExpressionParameter parameter : parameters) {
			parameterNames[i] = parameter.getName();
			parameterTypes[i] = parameter.getType();
			parameterValues[i] = parameter.getValue();
			i++;
		}
		return type.cast(doEvaluate(expression, type, parameterNames, parameterTypes, parameterValues));
	}

	private Object doEvaluate(final String expression, final Class<?> type, final String[] parameterNames, final Class<?>[] parameterTypes, final Object[] parameterValues) {
		final ScriptEvaluator scriptEvaluator;
		//1. Phase de construction du script
		try {
			scriptEvaluator = new ScriptEvaluator(expression, type, parameterNames, parameterTypes);
		} catch (final Exception ex) {
			throw new VRuntimeException("Erreur durant la construction du preprocessing de texte dynamique dans \n" + expression + '\n', ex);
		}

		//2. Phase d'�valuation du script
		try {
			return scriptEvaluator.evaluate(parameterValues);
		} catch (final InvocationTargetException e) {
			//On d�balle l'exception
			//Si l'exception originale est une KSystemException on la retourne
			//Sinon on l'embale dans une KSystemException
			final Throwable t = e.getCause();
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			if (t instanceof Error) {
				throw (Error) t;
			}
			throw new VRuntimeException("Erreur durant l'�valuation du script", t);
		}
	}
}

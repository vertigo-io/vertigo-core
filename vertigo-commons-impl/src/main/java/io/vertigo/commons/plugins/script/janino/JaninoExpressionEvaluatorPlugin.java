/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.commons.plugins.script.janino;

import io.vertigo.commons.impl.script.ExpressionEvaluatorPlugin;
import io.vertigo.commons.script.ExpressionParameter;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.codehaus.janino.ScriptEvaluator;

/**
 * Evaluate an expression using Janino.
 *
 * @author  pchretien
 */
public final class JaninoExpressionEvaluatorPlugin implements ExpressionEvaluatorPlugin {
	/** {@inheritDoc} */
	@Override
	public <J> J evaluate(final String expression, final List<ExpressionParameter> parameters, final Class<J> type) {
		//0. Init Janino parameters defined by their names, types and values.
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

		//1. Build the scriptEvaluator
		final ScriptEvaluator scriptEvaluator = buildEvaluator(expression, type, parameterNames, parameterTypes);

		//2.Evaluate the script
		return type.cast(doEvaluate(scriptEvaluator, parameterValues));
	}

	private static Object doEvaluate(final ScriptEvaluator scriptEvaluator, final Object[] parameterValues) {
		try {
			return scriptEvaluator.evaluate(parameterValues);
		} catch (final InvocationTargetException e) {
			//On déballe l'exception
			//Si l'exception originale est une KSystemException on la retourne
			//Sinon on l'embale dans une KSystemException
			final Throwable t = e.getCause();
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			if (t instanceof Error) {
				throw (Error) t;
			}
			throw new RuntimeException("Erreur durant l'évaluation du script", t);
		}
	}

	private static ScriptEvaluator buildEvaluator(final String expression, final Class<?> type, final String[] parameterNames, final Class<?>[] parameterTypes) {
		try {
			return new ScriptEvaluator(expression, type, parameterNames, parameterTypes);
		} catch (final Exception ex) {
			throw new RuntimeException("An error occurred during text preprocessing  in \n" + expression + '\n', ex);
		}
	}
}

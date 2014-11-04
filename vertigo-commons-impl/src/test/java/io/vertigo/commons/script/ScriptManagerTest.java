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
package io.vertigo.commons.script;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public final class ScriptManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ScriptManager scriptManager;
	private final ScriptSeparator comment = new ScriptSeparator("<!--", "-->");

	//	private ExpressionEvaluatorPlugin bsExpressionEvaluatorPlugin;
	//	private ExpressionEvaluatorPlugin mvelExpressionEvaluatorPlugin;

	private static List<ExpressionParameter> createParameters() {
		final List<ExpressionParameter> parameters = new ArrayList<>();
		parameters.add(new ExpressionParameter("nom", String.class, "Duraton"));
		parameters.add(new ExpressionParameter("prenom", String.class, "jean paul"));
		parameters.add(new ExpressionParameter("age", Integer.class, 54));
		return parameters;
	}

	@Test
	public void testStringReplace() {
		final String script = "ce matin M.<%=nom%> est allé chercher son pain";
		final String result = scriptManager.evaluateScript(script, SeparatorType.CLASSIC, createParameters());
		Assert.assertEquals("ce matin M.Duraton est allé chercher son pain", result);
	}

	@Test
	public void testIntegerReplace() {
		final String script = "M.Duraton a <%=age%> ans";
		final String result = scriptManager.evaluateScript(script, SeparatorType.CLASSIC, createParameters());
		Assert.assertEquals("M.Duraton a 54 ans", result);
	}

	@Test
	public void testCount() {
		final String script = "Le prénom de M.<%=nom%> est composé de <%=prenom.length()%> lettres";
		final String result = scriptManager.evaluateScript(script, SeparatorType.CLASSIC, createParameters());
		Assert.assertEquals("Le prénom de M.Duraton est composé de 9 lettres", result);
	}

	@Test
	public void testIf() {
		final String script = "<%if (nom.startsWith(\"Dur\")) {%>Il s'agit bien de M.Duraton<%}%>";
		final String result = scriptManager.evaluateScript(script, SeparatorType.CLASSIC, createParameters());
		Assert.assertEquals("Il s'agit bien de M.Duraton", result);
	}

	@Test(expected = Exception.class)
	public void testSyntaxError() {
		//On génère une erreur java
		final String script = "<%if (nom.sttart(\"Dur\")) {%>Il s'agit bien de M.Duraton<%}%>";
		final String result = scriptManager.evaluateScript(script, SeparatorType.CLASSIC, createParameters());
		nop(result);
	}

	@Test
	public void testNonDynamic() {
		//On fait une évaluation d'un texte qui s'avère non dynamique. (Absence de <%)
		final String script = "Il s'agit bien de M.Duraton";
		final String result = scriptManager.evaluateScript(script, SeparatorType.CLASSIC, createParameters());
		Assert.assertEquals("Il s'agit bien de M.Duraton", result);
	}

	@Test
	public void testComment() {
		final List<ScriptSeparator> separators = new ArrayList<>();
		separators.add(comment);

		final String script = "bla <!--commentaires-->bla";

		final MyScriptParserHandler scriptHandler = new MyScriptParserHandler();
		scriptManager.parse(script, scriptHandler, separators);
		Assert.assertEquals("bla bla", scriptHandler.result.toString());
	}

	@Test(expected = Exception.class)
	public void testParameterForgotten() {
		final List<ScriptSeparator> separators = new ArrayList<>();
		separators.add(comment);

		final String script = "bla <!---->bla";

		final MyScriptParserHandler scriptHandler = new MyScriptParserHandler();

		scriptManager.parse(script, scriptHandler, separators);
	}

	@Test
	public void testEchappement() {
		//Si le séparateur est un car.
		//il suffit de double le séparateur pour l'échapper.
		final List<ScriptSeparator> separators = new ArrayList<>();
		separators.add(new ScriptSeparator('$'));
		final String script = "le prix du barril est de $price$ $$";
		final MyScriptParserHandler scriptHandler = new MyScriptParserHandler();
		scriptManager.parse(script, scriptHandler, separators);
		Assert.assertEquals("le prix du barril est de 100 $", scriptHandler.result.toString());
	}

	@Test(expected = Exception.class)
	public void testOubliCaractereDeFin() {
		final List<ScriptSeparator> separators = new ArrayList<>();
		separators.add(new ScriptSeparator('$'));
		final String script = "le prix du barril est de $price";
		final MyScriptParserHandler scriptHandler = new MyScriptParserHandler();

		scriptManager.parse(script, scriptHandler, separators);
	}

	@Test
	public void testExpressionString() {
		final String test = scriptManager.evaluateExpression("\"darwin\"", createParameters(), String.class);
		Assert.assertEquals("darwin", test);
	}

	@Test
	public void testExpressionVarString() {
		final String test = scriptManager.evaluateExpression("nom", createParameters(), String.class);
		Assert.assertEquals("Duraton", test);
	}

	@Test
	public void testExpressionVarInteger() {
		final Integer test = scriptManager.evaluateExpression("age", createParameters(), Integer.class);
		Assert.assertEquals(54, test.intValue());
	}

	@Test
	public void testExpressionVarBoolean() {
		final Boolean test = scriptManager.evaluateExpression("age>20", createParameters(), Boolean.class);
		Assert.assertEquals(true, test.booleanValue());
	}

	//
	//	@Test
	//	public void testBSExpressionVarBoolean() {
	//		final Boolean test = bsExpressionEvaluatorPlugin.evaluate("age>20", createParameters(), Boolean.class);
	//		Assert.assertEquals(true, test.booleanValue());
	//	}
	//
	//	@Test
	//	public void testMVELExpressionVarBoolean() {
	//		final Boolean test = mvelExpressionEvaluatorPlugin.evaluate("nom == 'Duraton'", createParameters(), Boolean.class);
	//		Assert.assertEquals(true, test.booleanValue());
	//	}

	private class MyScriptParserHandler implements ScriptParserHandler {
		final StringBuilder result = new StringBuilder();

		public void onExpression(final String expression, final ScriptSeparator separator) {
			if ("price".equals(expression)) {
				result.append("100");
			} else if (!separator.equals(comment)) {
				result.append(expression);
			}
		}

		public void onText(final String text) {
			result.append(text);
		}
	}
}

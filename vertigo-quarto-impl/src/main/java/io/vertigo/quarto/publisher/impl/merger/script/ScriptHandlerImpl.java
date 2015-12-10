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
package io.vertigo.quarto.publisher.impl.merger.script;

import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.lang.WrappedException;

import java.util.Stack;

/**
 * Evaluation d'une grammaire de haut niveau.
 * Elle est définie par les tags suivants :
 * - for
 * - if
 * - = (écriture directe d'un champ)
 *
 * @author oboitel
 */
public final class ScriptHandlerImpl implements ScriptParserHandler {
	private final StringBuilder evaluatedScript = new StringBuilder();

	/*
	 * Controler les ouvertures et fermetures de blocs.
	 */
	private final Stack<TagStackEntry> blockStack = new Stack<>();

	private boolean isGrammarClosed;

	private final ScriptGrammar scriptGrammar;
	private final ScriptContext scriptContext;

	/**
	 * Constructeur.
	 * @param variableName Nom de la variable
	 * @param scriptGrammar KScriptGrammar
	 */
	public ScriptHandlerImpl(final String variableName, final ScriptGrammar scriptGrammar) {
		//-----
		scriptContext = new ScriptContextimpl();
		scriptContext.push(variableName);
		this.scriptGrammar = scriptGrammar;
	}

	private static class TagStackEntry {

		private final String name;
		private final ScriptTag tagHandler;

		TagStackEntry(final String name, final ScriptTag tagHandler) {
			this.name = name;
			this.tagHandler = tagHandler;
		}

		final ScriptTag getTagHandler() {
			return tagHandler;
		}

		final String getName() {
			return name;
		}
	}

	/**
	 * Fermeture d'un bloc défini par un mot clé de la grammaire.
	 * On verifie qu'un bloc a bien été ouvert et que tous ses blocs internes sont bien fermés.
	 * @param tagName nom du tag à dépiler
	 * @return Handler du tag pour ce tag (même instance que pour l'ouverture)
	 */
	private ScriptTag closeBlock(final String tagName) {
		Assertion.checkNotNull(tagName);
		//-----
		if (!blockStack.isEmpty()) {
			final TagStackEntry tagStackEntry = blockStack.pop();
			if (tagStackEntry.getName().equals(tagName)) {
				return tagStackEntry.getTagHandler();
			}
		}
		/*Cas de malformation des balises*/
		throw new VSystemException("bloc {0} mal forme : balise de fin manquante", tagName);
	}

	/**
	 * Ouverture d'un nouveau bloc défini par un mot clé de la grammaire.
	 * exemple : FOR, IF
	 * @param tagName nom du tag
	 * @param tagHandler instance du handler de tag
	 */
	private void openBlock(final String tagName, final ScriptTag tagHandler) {
		Assertion.checkNotNull(tagName);
		Assertion.checkNotNull(tagHandler);
		//-----
		blockStack.push(new TagStackEntry(tagName, tagHandler));
	}

	// ==========================================================================
	// traitement d'un tag
	// ==========================================================================
	/*
	 * 
	 * parse un Tag pour trouver son type et ses attributs ces deux informations
	 * sont ensuite stockées dans un objet de type TagContent
	 */

	/** {@inheritDoc} */
	@Override
	public void onText(final String text) {
		evaluatedScript.append(text);
	}

	/** {@inheritDoc} */
	@Override
	public void onExpression(final String expression, final ScriptSeparator separator) {
		Assertion.checkState(!isGrammarClosed, "l'évaluateur de grammaire ne peut pas être réutilisé car il a été fermé");
		Assertion.checkArgument(expression != null, "un tag ne doit pas etre vide");
		//-----
		final ScriptTagContent tagContent = scriptGrammar.parseTag(expression);
		tagContent.setCurrentVariable(scriptContext.peek());
		evaluatedScript.append(renderTag(tagContent));
	}

	private String renderTag(final ScriptTagContent tagContent) {
		final String result;
		final Boolean open = tagContent.getScriptTagDefinition().isOpenTag();
		final ScriptTag tagHandler;

		if (open == null) {
			tagHandler = createTagHandler(tagContent.getScriptTagDefinition());
			//ouvert et fermant
			result = tagHandler.renderOpen(tagContent, scriptContext) + tagHandler.renderClose(tagContent, scriptContext);
		} else if (open.booleanValue()) {
			//On push l'instance du handler de tag, pour réutiliser la même instance lors de la fermeture
			tagHandler = createTagHandler(tagContent.getScriptTagDefinition());
			openBlock(tagContent.getScriptTagDefinition().getName(), tagHandler);
			result = tagHandler.renderOpen(tagContent, scriptContext);
		} else { //si fermeture, on pop l'instance du handler utilisé lors de l'ouverture
			tagHandler = closeBlock(tagContent.getScriptTagDefinition().getName());
			result = tagHandler.renderClose(tagContent, scriptContext);
		}
		return result;
	}

	public String result() {
		if (!blockStack.isEmpty()) {
			throw new IllegalStateException("tous les blocs d'instructions doivent être fermés");
		}
		isGrammarClosed = true;
		return evaluatedScript.toString();
	}

	private static ScriptTag createTagHandler(final ScriptTagDefinition tagDefinition) {
		try {
			return tagDefinition.getClassTag().newInstance();
		} catch (final InstantiationException | IllegalAccessException e) {
			throw new WrappedException("Probleme a l'initialisation du tag personalise : " + tagDefinition.getName(), e);
		}
	}

	private static class ScriptContextimpl implements ScriptContext {
		/*
		 * Stocker les variables déclarées par la grammaire.
		 */
		private final Stack<String> variableNames = new Stack<>();

		@Override
		public String peek() {
			return variableNames.peek();
		}

		@Override
		public String pop() {
			return variableNames.pop();
		}

		@Override
		public void push(final String variableName) {
			variableNames.push(variableName);
		}

		@Override
		public boolean empty() {
			return variableNames.empty();
		}
	}
}

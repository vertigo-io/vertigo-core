package io.vertigo.publisher.impl.merger.script;

import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.util.Stack;

/**
 * Evaluation d'une grammaire de haut niveau.
 * Elle est d�finie par les tags suivants :
 * - for
 * - if
 * - = (�criture directe d'un champ)
 *
 * @author oboitel
 * @version $Id: ScriptHandlerImpl.java,v 1.7 2014/02/27 10:33:07 pchretien Exp $
 */
public final class ScriptHandlerImpl implements ScriptParserHandler {
	private final StringBuilder evaluatedScript = new StringBuilder();

	/*
	 * Controler les ouvertures et fermetures de blocs.
	 */
	private final Stack<TagStackEntry> blockStack = new Stack<>();

	private boolean isGrammarClosed; // initialis� � false

	private final ScriptGrammar scriptGrammar;
	private final ScriptContext scriptContext;

	/**
	 * Constructeur.
	 * @param variableName Nom de la variable
	 * @param scriptGrammar KScriptGrammar
	 */
	public ScriptHandlerImpl(final String variableName, final ScriptGrammar scriptGrammar) {
		Assertion.checkNotNull(scriptGrammar);
		//---------------------------------------------------
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
	 * Fermeture d'un bloc d�fini par un mot cl� de la grammaire.
	 * On verifie qu'un bloc a bien �t� ouvert et que tous ses blocs internes sont bien ferm�s.
	 * @param tagName nom du tag � d�piler
	 * @return Handler du tag pour ce tag (m�me instance que pour l'ouverture)
	 */
	private ScriptTag closeBlock(final String tagName) {
		Assertion.checkNotNull(tagName);
		//----------------------------------------
		if (!blockStack.isEmpty()) {
			final TagStackEntry tagStackEntry = blockStack.pop();
			if (tagStackEntry.getName().equals(tagName)) {
				return tagStackEntry.getTagHandler();
			}
		}
		//Cas de malformation des balises
		throw new VRuntimeException("bloc {0} mal form� : balise de fin manquante", null, tagName);
	}

	/**
	 * Ouverture d'un nouveau bloc d�fini par un mot cl� de la grammaire.
	 * exemple : FOR, IF
	 * @param tagName nom du tag
	 * @param tagHandler instance du handler de tag
	 */
	private void openBlock(final String tagName, final ScriptTag tagHandler) {
		Assertion.checkNotNull(tagName);
		Assertion.checkNotNull(tagHandler);
		//--------------------------------------
		blockStack.push(new TagStackEntry(tagName, tagHandler));
	}

	// ==========================================================================
	// traitement d'un tag
	// ==========================================================================
	/*
	 * parse un Tag pour trouver son type et ses attributs ces deux informations
	 * sont ensuite stock�es dans un objet de type TagContent
	 */

	/** {@inheritDoc} */
	public void onText(final String text) {
		evaluatedScript.append(text);
	}

	/** {@inheritDoc} */
	public void onExpression(final String expression, final ScriptSeparator separator) {
		if (isGrammarClosed) {
			throw new IllegalStateException("l'�valuateur de grammaire ne peut pas �tre r�utilis� car il a �t� ferm�");
		}

		if (expression == null) {
			throw new IllegalArgumentException("un tag ne doit pas etre vide");
		}

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
			//On push l'instance du handler de tag, pour r�utiliser la m�me instance lors de la fermeture
			tagHandler = createTagHandler(tagContent.getScriptTagDefinition());
			openBlock(tagContent.getScriptTagDefinition().getName(), tagHandler);
			result = tagHandler.renderOpen(tagContent, scriptContext);
		} else { //si fermeture, on pop l'instance du handler utilis� lors de l'ouverture
			tagHandler = closeBlock(tagContent.getScriptTagDefinition().getName());
			result = tagHandler.renderClose(tagContent, scriptContext);
		}
		return result;
	}

	public String result() {
		if (!blockStack.isEmpty()) {
			throw new IllegalStateException("tous les blocs d'instructions doivent �tre ferm�s");
		}
		isGrammarClosed = true;
		return evaluatedScript.toString();
	}

	private ScriptTag createTagHandler(final ScriptTagDefinition tagDefinition) {
		try {
			return tagDefinition.getClassTag().newInstance();
		} catch (final InstantiationException e) {
			throw new VRuntimeException("Probl�me � l'initialisation du tag personalis� : " + tagDefinition.getName(), e);
		} catch (final IllegalAccessException e) {
			throw new VRuntimeException("Probl�me � l'initialisation du tag personalis� : " + tagDefinition.getName(), e);
		}
	}

	private static class ScriptContextimpl implements ScriptContext {
		/*
		 * Stocker les variables d�clar�es par la grammaire.
		 */
		private final Stack<String> variableNames = new Stack<>();

		public String peek() {
			return variableNames.peek();
		}

		public String pop() {
			return variableNames.pop();
		}

		public void push(final String variableName) {
			variableNames.push(variableName);
		}

		public boolean empty() {
			return variableNames.empty();
		}
	}
}

package io.vertigo.quarto.publisher.impl.merger.processor;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptGrammar;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptHandlerImpl;
import io.vertigo.quarto.publisher.model.PublisherData;

/**
 * Processor String2String qui evalue la chaine d'entree en transformant les balises
 * de la grammaire ODT en balise jsp <% %>.
 * Les balises reconnues sont <# #>
 * @author oboitel
 */
public final class GrammarEvaluatorProcessor implements MergerProcessor {
	private final ScriptManager scriptManager;
	private final ScriptGrammar scriptGrammar;

	/**
	 * Constructeur.
	 * @param scriptManager Manager des scripts
	 * @param scriptGrammar ScriptGrammar
	 */
	public GrammarEvaluatorProcessor(final ScriptManager scriptManager, final ScriptGrammar scriptGrammar) {
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(scriptGrammar);
		//---------------------------------------------------------------------
		this.scriptManager = scriptManager;
		this.scriptGrammar = scriptGrammar;
	}

	/** {@inheritDoc} */
	public String execute(final String input, final PublisherData publisherData) {
		final ScriptHandlerImpl scriptHandler = new ScriptHandlerImpl(MergerScriptEvaluatorProcessor.DATA, scriptGrammar);
		scriptManager.parse(input, scriptHandler, SeparatorType.XML_CODE.getSeparators());
		return scriptHandler.result();
	}
}

package io.vertigo.quarto.publisher.impl.merger.processor;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.publisher.impl.merger.grammar.TagEncodedField;
import io.vertigo.quarto.publisher.model.PublisherData;
import io.vertigo.quarto.publisher.model.PublisherNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Processor String2String qui 'evalue la chaine d'entr�e comme un script java.
 * Les balises reconnues sont <% %> et <%= %>
 * @author npiedeloup
 * @version $Id: MergerScriptEvaluatorProcessor.java,v 1.6 2014/02/27 10:32:26 pchretien Exp $
 */
public final class MergerScriptEvaluatorProcessor implements MergerProcessor {
	public static final String DATA = "data";
	private final ScriptManager scriptManager;
	private final Encoder<String, String> valueEncoder;

	/**
	 * Constructeur.
	 */
	public MergerScriptEvaluatorProcessor(final ScriptManager scriptManager, final Encoder<String, String> valueEncoder) {
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(valueEncoder);
		//---------------------------------------------------------------------
		this.scriptManager = scriptManager;
		this.valueEncoder = valueEncoder;
	}

	/** {@inheritDoc} */
	public String execute(final String script, final PublisherData publisherData) {
		final List<ExpressionParameter> scriptEvaluatorParameters = new ArrayList<>();

		final Class<PublisherNode> dataClass = PublisherNode.class;
		final ExpressionParameter data = new ExpressionParameter(DATA, dataClass, publisherData.getRootNode());
		final ExpressionParameter encoder = new ExpressionParameter(TagEncodedField.ENCODER, valueEncoder.getClass(), valueEncoder);
		scriptEvaluatorParameters.add(data);
		scriptEvaluatorParameters.add(encoder);

		return scriptManager.evaluateScript(script, SeparatorType.XML, scriptEvaluatorParameters);
	}
}

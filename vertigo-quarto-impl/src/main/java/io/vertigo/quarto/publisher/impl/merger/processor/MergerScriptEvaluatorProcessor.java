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
package io.vertigo.quarto.publisher.impl.merger.processor;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.core.lang.Assertion;
import io.vertigo.quarto.publisher.impl.merger.grammar.TagEncodedField;
import io.vertigo.quarto.publisher.model.PublisherData;
import io.vertigo.quarto.publisher.model.PublisherNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Processor String2String qui 'evalue la chaine d'entrée comme un script java.
 * Les balises reconnues sont <% %> et <%= %>
 * @author npiedeloup
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

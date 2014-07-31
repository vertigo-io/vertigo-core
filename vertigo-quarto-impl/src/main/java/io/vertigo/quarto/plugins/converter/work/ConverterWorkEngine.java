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
package io.vertigo.quarto.plugins.converter.work;

import io.vertigo.dynamo.work.WorkEngine;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.converter.ConverterManager;

import javax.inject.Inject;

/**
 * WorkEngine de conversion de document. 
 * Reentre sur le manager. 
 * Attention à utiliser un plugin qui effectue directement la conversion sans work, pour ne pas boucler. 
 * @author npiedeloup
 * @version $Id: ConverterWorkEngine.java,v 1.4 2014/01/20 18:56:52 pchretien Exp $
 */
public final class ConverterWorkEngine implements WorkEngine<KFileSerializable, ConverterWork> {
	private final ConverterManager converterManager;

	/**
	 * Constructeur.
	 * @param converterManager Manager de conversion
	 */
	@Inject
	public ConverterWorkEngine(final ConverterManager converterManager) {
		Assertion.checkNotNull(converterManager);
		//-----------------------------------------------------------------
		this.converterManager = converterManager;
	}

	/** {@inheritDoc} */
	public KFileSerializable process(final ConverterWork work) {
		return new KFileSerializable(converterManager.convert(work.getInputFile(), work.geTargetFormat()));
	}
}

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
package io.vertigo.quarto.publisher.impl.merger.grammar;

import io.vertigo.kernel.util.StringUtil;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptContext;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTag;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTagContent;

/**
 * @author pchretien, npiedeloup
 * @version $Id: TagBlock.java,v 1.2 2013/10/22 10:49:59 pchretien Exp $
 */
//public car instancié dynamiquement
public final class TagBlock extends AbstractKScriptTag implements ScriptTag {
	/** {@inheritDoc} */
	public String renderOpen(final ScriptTagContent tag, final ScriptContext context) {
		return START_BLOC_JSP + decode(tag.getAttribute()) + END_BLOC_JSP;
	}

	/** {@inheritDoc} */
	public String renderClose(final ScriptTagContent tag, final ScriptContext context) {
		return START_BLOC_JSP + decode(tag.getAttribute()) + END_BLOC_JSP;
	}

	private String decode(final String s) {
		//On décode les caractères qui peuvent avoir du sens dans un block
		final StringBuilder decode = new StringBuilder(s);
		StringUtil.replace(decode, "&quot;", "\"");
		StringUtil.replace(decode, "&lt;", "<");
		StringUtil.replace(decode, "&gt;", ">");
		return decode.toString();
	}
}

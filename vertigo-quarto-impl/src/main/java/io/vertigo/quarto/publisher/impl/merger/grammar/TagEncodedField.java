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

import io.vertigo.quarto.publisher.impl.merger.script.ScriptContext;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTag;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTagContent;

/**
 * @author pchretien, npiedeloup
 * @version $Id: TagEncodedField.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
//public car instancié dynamiquement
public final class TagEncodedField extends AbstractKScriptTag implements ScriptTag {
	public static final String ENCODER = "encoder";

	private static final String CALL = "=" + ENCODER + ".encode({0})";

	/** {@inheritDoc} */
	public String renderOpen(final ScriptTagContent tag, final ScriptContext context) {
		final String[] parsing = parseAttribute(tag.getAttribute(), FIELD_PATH_CALL);
		// le tag est dans le bon format
		parsing[0] = getCallForFieldPath(parsing[0], tag.getCurrentVariable());

		return getTagRepresentation(CALL, parsing);
	}

	/** {@inheritDoc} */
	public String renderClose(final ScriptTagContent tag, final ScriptContext context) {
		return "";
	}
}

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

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptContext;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTag;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTagContent;

/**
 * @author pchretien, npiedeloup
 * @version $Id: TagObject.java,v 1.3 2013/10/22 10:49:59 pchretien Exp $
 */
//public car instanci� dynamiquement
public final class TagObject extends AbstractKScriptTag implements ScriptTag {
	private static final String OBJECT_CALL = "\\{ {1} {2} = ({1}) {0}; ";
	private static final String OBJECT_ATTRIBUTE = "^([0-9a-zA-Z_]+) *= *([0-9a-zA-Z_]+(\\.[0-9a-zA-Z_]+)*)";

	/** {@inheritDoc} */
	public String renderOpen(final ScriptTagContent tag, final ScriptContext context) {
		// Renvoie un tableau de trois elements d'apr�s l'expression reguliere
		final String[] parsing = parseAttribute(tag.getAttribute(), OBJECT_ATTRIBUTE);

		// le tag est dans le bon format

		/**
		 * on recupere le parsing de l'expression reguliere
		 * parsing[0] -> attribut entier
		 * parsing[1] -> le nom de la variable de l'objet
		 * parsing[2] -> le field path de l'objet
		 */
		context.push(parsing[1]);

		// rendu du tag
		final String[] rendering = new String[3];
		rendering[0] = getCallForObjectFieldPath(parsing[2], tag.getCurrentVariable());
		rendering[1] = getDataAccessorClass().getCanonicalName();
		rendering[2] = parsing[1];

		return getTagRepresentation(OBJECT_CALL, rendering);
	}

	/** {@inheritDoc} */
	public String renderClose(final ScriptTagContent content, final ScriptContext context) {
		if (context.empty()) {
			throw new VRuntimeException("document malform� : le tag object est mal ferm�", null);
		}
		context.pop();
		return START_BLOC_JSP + '}' + END_BLOC_JSP;
	}
}

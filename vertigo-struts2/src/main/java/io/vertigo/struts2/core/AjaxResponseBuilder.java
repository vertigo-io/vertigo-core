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
package io.vertigo.struts2.core;

import io.vertigo.lang.Assertion;

import java.io.PrintWriter;

/**
 * Builder d'envoi de contenu Ajax.
 * @author npiedeloup
 */
public final class AjaxResponseBuilder {

	private boolean empty = true;
	private final boolean useBuffer;
	private final StringBuilder sb;
	private final PrintWriter writer;

	/**
	 * Constructeur.
	 * @param writer writer de la response
	 * @param useBuffer Si l'on utilise un buffer avant envoi
	 */
	AjaxResponseBuilder(final PrintWriter writer, final boolean useBuffer) {
		Assertion.checkNotNull(writer);
		//-----
		this.writer = writer;
		this.useBuffer = useBuffer;
		sb = useBuffer ? new StringBuilder() : null;
	}

	/**
	 * Envoi les données au client.
	 * @return Retour de l'action struts
	 */
	public String send() {
		if (useBuffer) {
			writer.write(sb.toString());
			sb.setLength(0);
		}
		if (empty) {
			writer.write("<emptyAjaxResponse/>");
		}
		return null;
	}

	/**
	 * Envoi du contenu HTML brut.
	 * Les elements HTML identifi�s sont remplacés, le code script est interpr�té.
	 * @param content Contenu.
	 * @return AjaxResponseBuilder
	 */
	public AjaxResponseBuilder appendHtml(final String content) {
		Assertion.checkNotNull(content);
		//-----
		if (useBuffer) {
			sb.append("<htmlUpdate>");
			sb.append(content);
			sb.append("</htmlUpdate>");
		} else {
			writer.write("<htmlUpdate>");
			writer.write(content);
			writer.write("</htmlUpdate>");
		}
		empty = false;
		return this;
	}

	/**
	 * Envoi du contenu Json brut.
	 * @param json Json.
	 * @return AjaxResponseBuilder
	 */
	public AjaxResponseBuilder withJson(final String json) {
		if (useBuffer) {
			sb.append(json);
		} else {
			writer.write(json);
		}
		empty = false;
		return this;
	}
}

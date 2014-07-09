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

/**
 * Handler du parsing XML.
 * @author npiedeloup
 * @version $Id: ParserXMLHandler.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
public interface ParserXMLHandler {
	/**
	 * Appell� sur un tag sans body.
	 * @param tagXML Extrait du tag XML complet
	 * @param output Flux d'ecriture, il contient tout jusqu'au caract�re pr�c�dant le tag lui m�me
	 */
	void onNoBodyEndTag(final String tagXML, final StringBuilder output);

	/**
	 * Appell� sur un tag sans body.
	 * @param tagXML Extrait du tag XML complet (ie : avec son Body)
	 * @param bodyContent Body du tag
	 * @param output Flux d'ecriture, il contient tout jusqu'au caract�re pr�c�dant le tag lui m�me
	 */
	void onBodyEndTag(final String tagXML, final String bodyContent, final StringBuilder output);

}
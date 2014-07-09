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
package io.vertigo.quarto.impl.converter;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.component.Plugin;

/**
 * Plugin de Conversion des fichiers.
 * 
 * @author npiedeloup
 * @version $Id: ConverterPlugin.java,v 1.3 2014/01/28 18:49:24 pchretien Exp $
 */
public interface ConverterPlugin extends Plugin {
	/**
	 * Retourne le fichier converti
	 * L'appel � l'OOO distant est synchroniz�, car il supporte mal les converssions concurrentes.
	 * @param file Fichier � convertir
	 * @param targetFormat Type de document de sortie ODT,RTF,DOC,CSV,PDF
	 * @return Fichier converti
	 */
	KFile convertToFormat(final KFile file, final String targetFormat);

}

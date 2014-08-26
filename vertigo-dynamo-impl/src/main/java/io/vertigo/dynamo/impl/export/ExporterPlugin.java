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
package io.vertigo.dynamo.impl.export;

import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportFormat;
import io.vertigo.kernel.component.Plugin;

import java.io.OutputStream;

/**
 * Plugin de l'export.
 * Les paramètres qui lui sont associés permettent d'agir sur le resultat de l'export.
 * Le plugin accepte toutes les exceptions afin de centraliser leur gestion en un seul endroit.
 *
 * @author pchretien, npiedeloup
 */
public interface ExporterPlugin extends Plugin {
	/**
	 * Réalise l'export des données de contenu.
	 * @param metaParameters paramètres de cet export
	 * @param out Le flux d'écriture des données exportées.
	 * @throws Exception Exception générique en cas d'erreur lors de l'export
	 */
	void exportData(final Export metaParameters, final OutputStream out) throws Exception;

	/**
	 * Type de Format accepté à l'export
	 * @param exportFormat
	 * @return si le format précisé est pris en compte par le plugin
	 */
	boolean accept(ExportFormat exportFormat);

}

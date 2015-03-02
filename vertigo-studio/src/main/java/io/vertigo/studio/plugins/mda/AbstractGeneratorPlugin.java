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
package io.vertigo.studio.plugins.mda;

import io.vertigo.studio.impl.mda.GeneratorPlugin;

import java.io.File;
import java.util.Map;

/**
 * Pré-ImplémGénération de la génération des fichiers.
 *
 * @author dchallas
 */
public abstract class AbstractGeneratorPlugin implements GeneratorPlugin {

	/**
	 * @param fileGeneratorConfig Configuration de la génération
	 * @param mapRoot context
	 * @param classSimpleName className
	 * @param genSubDir Nom subdir de génération
	 * @param packageName Nom du package
	 * @param fileExtention Extension du ficher (sql, java...)
	 * @param templateName Nom du template
	 * @return Générateur de fichier
	 */
	protected static final FileGenerator createFileGenerator(final FileConfig fileGeneratorConfig, final Map<String, Object> mapRoot, final String classSimpleName, final String genSubDir, final String packageName, final String fileExtention, final String templateName) {
		return new FileGeneratorFreeMarker(mapRoot, classSimpleName, packageName, fileExtention, templateName, fileGeneratorConfig.getTargetGenDir() + genSubDir + File.separatorChar, fileGeneratorConfig.getEncoding(), fileGeneratorConfig.getClass());
	}
}

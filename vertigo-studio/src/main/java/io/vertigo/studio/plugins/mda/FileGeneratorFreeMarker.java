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

import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.Result;
import io.vertigo.studio.plugins.mda.domain.TemplateMethodStringUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Génération des fichiers avec FreeMarker.
 *
 * @author dchallas
 */
public final class FileGeneratorFreeMarker implements FileGenerator {

	/**
	 * Répertoire des fichiers générés une fois.
	 * Doit être renseigné dans le fichier properties [targetDir]
	 */
	private final String targetDir;
	/**
	 * Répertoire des fichiers TOUJOURS générés
	 * Doit être renseigné dans le fichier properties [targetDir]
	 */
	private final String targetGenDir;
	private final Configuration configuration;
	private final Map<String, Object> mapRoot;
	private final String classSimpleName;
	private final String packageName;
	private final String fileExtention;
	private final String templateName;
	private final String encoding;

	/**
	 * Constructeur.
	 *
	 * @param parameters Paramètres de génération des fichiers java
	 * @param mapRoot context
	 * @param classSimpleName className
	 * @param packageName Nom du package
	 * @param fileExtention Extension du ficher (sql, java...)
	 * @param templateName Nom du template
	 */
	public FileGeneratorFreeMarker(final AbstractConfiguration parameters, final Map<String, Object> mapRoot, final String classSimpleName, final String packageName, final String fileExtention, final String templateName) {
		Assertion.checkNotNull(parameters);
		Assertion.checkNotNull(mapRoot);
		Assertion.checkNotNull(classSimpleName);
		Assertion.checkNotNull(packageName);
		Assertion.checkNotNull(fileExtention);
		Assertion.checkNotNull(templateName);
		// ---------------------------------------------------------------------
		this.mapRoot = mapRoot;
		this.classSimpleName = classSimpleName;
		this.packageName = packageName;
		this.fileExtention = fileExtention;
		this.templateName = templateName;
		configuration = initConfiguration(parameters.getClass());
		targetDir = parameters.getTargetDir();
		targetGenDir = parameters.getTargetGenDir();
		encoding = parameters.getEncoding();
	}

	/**
	 * @param referenceClass Class de référence du template
	 * @return Configuration de FreeMarker
	 */
	private Configuration initConfiguration(final Class<?> referenceClass) {
		final Configuration config = new Configuration();
		config.setSharedVariable("constToCamelCase", new TemplateMethodStringUtil());
		setTemplateLoading(config, referenceClass);
		config.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
		return config;
	}

	private void setTemplateLoading(final Configuration config, final Class<?> referenceClass) {
		config.setClassForTemplateLoading(referenceClass, "");
	}

	/** {@inheritDoc} */
	@Override
	public void generateFile(final Result result, final boolean override) {
		final File file = new File(getFileName(override));
		if (override || !file.exists()) {
			try {
				generateFile(result, file);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static String package2directory(final String packageName) {
		return packageName.replace('.', '/').replace('\\', '/');
	}

	private String getFileName(final boolean override) {
		final String finalTargetDir = override ? targetGenDir : targetDir;
		final String currentPath = finalTargetDir + package2directory(packageName);
		return currentPath + '/' + classSimpleName + fileExtention;
	}

	private void generateFile(final Result result, final File file2create) throws IOException, TemplateException {
		// On crée le répertoire
		final File directory2create = file2create.getParentFile();
		directory2create.mkdirs();
		// Cette ligne doit se trouver avant le writer car si le fichier est
		// deja ouvert en ecriture il y a pb, on ne peut pas lire le code
		// deja existant
		final String content = buildContentFile();
		// pour optimisation de l'écriture et de la compilation,
		// on vérifie qu'on ne réécrit pas exactement la même chose que ce
		// qu'il y a déjà
		final String currentContent = FileUtil.readContentFile(file2create, encoding);
		if (content.equals(currentContent)) {
			// Les deux fichiers sont identiques
			result.addIdenticalFile(file2create);
		} else {
			// Si le contenu est différent on réécrit le fichier.
			final boolean success = FileUtil.writeFile(file2create, content, encoding);
			result.addFileWritten(file2create, success);
		}
	}

	/**
	 * Crée le contenu d'un fichier.
	 *
	 * @return Contenu du fichier
	 */
	private String buildContentFile() throws IOException, TemplateException {
		// Si le fichier existe on le remplace par le fichier créé.
		// Si le fichier n'existe pas on en crée un.
		final StringWriter writer = new StringWriter(); // Il est inutile de fermer une StringWriter.
		// Génération du contenu du fichier.
		// Vertigo étant en UTF-8, les fichiers ftl doivent être lu en UTF-8
		final Template template = configuration.getTemplate(templateName, "UTF-8");
		template.process(mapRoot, writer);
		return writer.toString();
	}
}

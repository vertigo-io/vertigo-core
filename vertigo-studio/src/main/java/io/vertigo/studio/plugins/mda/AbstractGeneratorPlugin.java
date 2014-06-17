package io.vertigo.studio.plugins.mda;

import io.vertigo.studio.mda.Configuration;
import io.vertigo.studio.mda.GeneratorPlugin;

import java.util.Map;

/**
 * Pré-ImplémGénération de la génération des fichiers.
 *  
 * @author dchallas
 * @param <C> Class de configuration du générateur
 */
public abstract class AbstractGeneratorPlugin<C extends Configuration> implements GeneratorPlugin<C> {

	/**
	 * @param fileGeneratorConfiguration Configuration de la génération
	 * @param mapRoot context
	 * @param classSimpleName className
	 * @param packageName Nom du package 
	 * @param fileExtention Extension du ficher (sql, java...)
	 * @param templateName Nom du template
	 * @return Générateur de fichier
	 */
	protected final FileGenerator getFileGenerator(final AbstractConfiguration fileGeneratorConfiguration, final Map<String, Object> mapRoot, final String classSimpleName, final String packageName, final String fileExtention, final String templateName) {
		return new FileGeneratorFreeMarker(fileGeneratorConfiguration, mapRoot, classSimpleName, packageName, fileExtention, templateName);
	}
}

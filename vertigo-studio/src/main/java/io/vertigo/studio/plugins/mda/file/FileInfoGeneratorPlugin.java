package io.vertigo.studio.plugins.mda.file;

import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.mda.Result;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.studio.plugins.mda.FileGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Génération des objets relatifs au module File. 
 *  
 * @author npiedeloup
 * @version $Id: FileInfoGeneratorPlugin.java,v 1.5 2014/02/27 10:29:55 pchretien Exp $
 */
public final class FileInfoGeneratorPlugin extends AbstractGeneratorPlugin<FileInfoConfiguration> {
	/** {@inheritDoc} */
	public FileInfoConfiguration createConfiguration(final Properties properties) {
		return new FileInfoConfiguration(properties);
	}

	/** {@inheritDoc} */
	public void generate(final FileInfoConfiguration fileInfoConfiguration, final Result result) {
		Assertion.checkNotNull(fileInfoConfiguration);
		Assertion.checkNotNull(result);
		// ---------------------------------------------------------------------
		/* Générations des FI. */
		generateFileInfos(fileInfoConfiguration, result);
	}

	private void generateFileInfos(final FileInfoConfiguration fileInfoConfiguration, final Result result) {
		final Collection<FileInfoDefinition> fileInfoDefinitions = Home.getDefinitionSpace().getAll(FileInfoDefinition.class);
		for (final FileInfoDefinition fileInfoDefinition : fileInfoDefinitions) {
			generateFileInfo(fileInfoConfiguration, result, fileInfoDefinition);
		}
	}

	private void generateFileInfo(final FileInfoConfiguration fileInfoConfiguration, final Result result, final FileInfoDefinition fileInfoDefinition) {
		final TemplateFileInfoDefinition definition = new TemplateFileInfoDefinition(fileInfoDefinition);
		final Map<String, Object> mapRoot = new HashMap<>();
		mapRoot.put("fiDefinition", definition);
		mapRoot.put("packageName", fileInfoConfiguration.getFilePackage());

		final FileGenerator super2java = getFileGenerator(fileInfoConfiguration, mapRoot, definition.getClassSimpleName(), fileInfoConfiguration.getFilePackage(), //
				".java", "file/fileInfo.ftl");
		super2java.generateFile(result, true);
	}
}

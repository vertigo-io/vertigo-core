package io.vertigo.studio.plugins.mda.file;

import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionUtil;
import io.vertigo.kernel.util.StringUtil;

/**
 * Génération des classes/méthodes des fileInfo.
 * 
 * @author npiedeloup
 * @version $Id: TemplateFileInfoDefinition.java,v 1.4 2014/01/28 18:49:55 pchretien Exp $
 */
public final class TemplateFileInfoDefinition {
	private final FileInfoDefinition fileInfoDefinition;

	TemplateFileInfoDefinition(final FileInfoDefinition fileInfoDefinition) {
		Assertion.checkNotNull(fileInfoDefinition);
		//-----------------------------------------------------------------
		this.fileInfoDefinition = fileInfoDefinition;
	}

	/**
	 * @return Urn de la fileInfoDefinition
	 */
	public String getUrn() {
		return fileInfoDefinition.getName();
	}

	/**
	 * @return Nom de la class en CamelCase
	 */
	public String getClassSimpleName() {
		final String localName = DefinitionUtil.getLocalName(fileInfoDefinition.getName(), FileInfoDefinition.class);
		return StringUtil.constToCamelCase(localName, true);
	}
}

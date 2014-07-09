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
package io.vertigo.dynamo.file.metamodel;

import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.DefinitionUtil;
import io.vertigo.kernel.metamodel.Prefix;
import io.vertigo.kernel.util.StringUtil;

/**
 * Définition d'un FileInfo.
 *
 * La définition n'est pas serializable.
 * Elle doit être invariante (non mutable) dans le temps.
 * Par défaut elle est chargée au (re)démarrage du serveur.
 *
 * @author  npiedeloup, pchretien
 */
@Prefix("FI")
public final class FileInfoDefinition implements Definition {
	/**
	 * Nom de la définition.
	 */
	private final String name;
	/**
	 * Racine des fichiers de ce type (utilisé par le store).
	 */
	private final String root;

	/**
	 * Nom du fileStorePlugin utilisé.
	 * On garde le nom et pas le plugin qui n'est porté que par le FileManager.
	 */
	private final String fileStoreName;

	/**
	 * Constructeur.
	 * @param root Racine des fichiers de ce type
	 * @param fileStoreName Nom du fileStorePlugin utilisé
	 */
	public FileInfoDefinition(final String name, final String root, final String fileStoreName) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(root);
		Assertion.checkNotNull(fileStoreName);
		//---------------------------------------------------------------------
		this.name = name;
		this.root = root;
		this.fileStoreName = fileStoreName;
	}

	/**
	 * @return Racine d'accès aux FI (utilisation depends du fileStorePlugin utilisé).
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * @return Nom du fileStorePlugin utilisé pour cette definition.
	 */
	public String getFileStorePluginName() {
		return fileStoreName;
	}

	/** {@inheritDoc} */
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}

	//=========================================================================
	//===========================STATIC========================================
	//=========================================================================
	public static FileInfoDefinition findFileInfoDefinition(final Class<? extends FileInfo> fileInfoClass) {
		Assertion.checkNotNull(fileInfoClass);
		//----------------------------------------------------------------------
		final String name = DefinitionUtil.getPrefix(FileInfoDefinition.class) + SEPARATOR + StringUtil.camelToConstCase(fileInfoClass.getSimpleName());
		return Home.getDefinitionSpace().resolve(name, FileInfoDefinition.class);
	}
}

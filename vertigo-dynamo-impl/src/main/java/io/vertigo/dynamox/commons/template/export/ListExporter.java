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
package io.vertigo.dynamox.commons.template.export;

import java.util.List;
import java.util.Map;

/**
 * Interface que doivent impl�menter les contr�leurs poss�dant une action d'export.
 * 
 * @version $Id: ListExporter.java,v 1.1 2013/07/10 15:46:44 npiedeloup Exp $
 */
public interface ListExporter {

	/**
	 * Retourne le nom du fichier r�sultat de l'export.
	 * 
	 * @return nom du fichier r�sultat de l'export
	 */
	String getFileNameToExport();

	/**
	 * Retourne le nom du document r�sultat de l'export.<br/>
	 * 
	 * @return nom du document r�sultat de l'export
	 */
	String getDocumentTitle();

	/**
	 * Get the list of the columns that must be exported.
	 * 
	 * @return list of the columns that must be exported
	 */
	List<String> getColumnNameListToExport();

	/**
	 * Get the list of the columns that must be excluded when exporting the criterion.
	 * 
	 * @return list of the columns that must be excluded when exporting the criterion
	 */
	List<String> getExcludedCriterionColumnNameList();

	/**
	 * Get the map giving specific label to use for the column.
	 * 
	 * @return Map containing the specific label, or null if no specific label are needed
	 */
	Map<String, String> getSpecificLabelMap();

}

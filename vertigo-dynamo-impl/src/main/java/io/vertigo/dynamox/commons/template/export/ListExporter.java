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

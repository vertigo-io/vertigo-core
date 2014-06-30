package io.vertigo.dynamo.export;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.kernel.lang.MessageText;

/**
 * Parametre d'export pour les donn�es de type DT.
 * La particularit� est que l'on fournit la liste des colonnes du DT a exporter,
 * avec �ventuellement des param�tres d'affichage particulier pour une colonne.
 * @author npiedeloup
 * @version $Id: ExportDtParameters.java,v 1.3 2014/01/20 17:49:10 pchretien Exp $
 */
public interface ExportDtParameters {
	/**
	 * Ajoute un champs du Dt dans l'export, le label de la colonne sera celui indiqu� dans le DT pour ce champs.
	 * @param exportfield ajout d'un champs du Dt � exporter
	 */
	void addExportField(final DtField exportfield);

	/**
	 * @param exportfield ajout d'un champs du Dt � exporter
	 * @param label nom sp�cifique � utiliser dans l'export, null si l'on souhaite utiliser celui indiqu� dans le DT pour ce champs
	 */
	void addExportField(final DtField exportfield, final MessageText label);

	/**
	 * Ajoute un champs du Dt dans l'export, le label de la colonne sera celui indiqu� dans le DT pour ce champs.
	 * @param exportfield ajout d'un champs du Dt � exporter
	 * @param list Liste des �l�ments d�norm�s
	 * @param displayfield Field du libell� � utiliser.
	 */
	void addExportDenormField(final DtField exportfield, final DtList<?> list, final DtField displayfield);

	/**
	 * @param exportfield ajout d'un champs du Dt � exporter
	 * @param list Liste des �l�ments d�norm�s
	 * @param displayfield Field du libell� � utiliser.
	 * @param label nom sp�cifique � utiliser dans l'export, null si l'on souhaite utiliser celui indiqu� dans le DT pour ce champs
	 */
	void addExportDenormField(final DtField exportfield, final DtList<?> list, final DtField displayfield, final MessageText label);

}

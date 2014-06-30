package io.vertigo.dynamo.export;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

import java.util.List;

/**
 * Interface de consultation des ExportDtParameters.
 *
 * @author npiedeloup
 * @version $Id: ExportDtParametersReadable.java,v 1.2 2014/01/20 17:49:10 pchretien Exp $
 */
public interface ExportDtParametersReadable {

	// NULL
	/**
	 * @return titre de cet objet/liste
	 */
	String getTitle();

	/**
	 * @return Liste des informations sur les Fields � exporter
	 */
	List<ExportField> getExportFields();

	/**
	 * @return Donn�es sous forme d'un DTO, ceci est un cast donc il y a l�v� d'une assertion si ce n'est pas un DTO
	 */
	DtObject getDtObject();

	/**
	 * @return Donn�es sous forme d'une DTC, ceci est un cast donc il y a l�v� d'une assertion si ce n'est pas une DTC
	 */
	DtList<?> getDtList();

	/**
	 * Le param�tre contient soit un DTO, soit une DTC.
	 * @return boolean true, si il contient un DTO
	 */
	boolean hasDtObject();
}

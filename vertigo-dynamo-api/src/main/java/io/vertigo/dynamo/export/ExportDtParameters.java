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
package io.vertigo.dynamo.export;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

import java.util.List;

/**
 * Interface de consultation des ExportDtParameters.
 *
 * @author npiedeloup
 */
public interface ExportDtParameters {

	// NULL
	/**
	 * @return titre de cet objet/liste
	 */
	String getTitle();

	/**
	 * @return Liste des informations sur les Fields à exporter
	 */
	List<ExportField> getExportFields();

	/**
	 * @return Données sous forme d'un DTO, ceci est un cast donc il y a levé d'une assertion si ce n'est pas un DTO
	 */
	DtObject getDtObject();

	/**
	 * @return Données sous forme d'une DTC, ceci est un cast donc il y a levé d'une assertion si ce n'est pas une DTC
	 */
	DtList<?> getDtList();

	/**
	 * Le paramètre contient soit un DTO, soit une DTC.
	 * @return boolean true, si il contient un DTO
	 */
	boolean hasDtObject();
}

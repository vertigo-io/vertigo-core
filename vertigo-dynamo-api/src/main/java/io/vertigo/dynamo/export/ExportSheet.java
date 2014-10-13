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

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface de consultation des ExportDtParameters.
 *
 * @author npiedeloup
 */
public final class ExportSheet {
	/**
	 * List des champs à exporter
	 */
	private final List<ExportField> exportFields;

	/**
	 * Objet à exporter.
	 * dto XOR dtc est renseigné.
	 */
	private final DtObject dto;
	private final DtList<?> dtc;

	private final String title;

	public ExportSheet(final String title, final List<ExportField> exportFields, final DtObject dto, final DtList dtc) {
		Assertion.checkNotNull(exportFields);
		Assertion.checkArgument(dto == null ^ dtc == null, "a dto or a dtc is required");
		//title may be null
		//---------------------------------------------------------------------
		this.exportFields = java.util.Collections.unmodifiableList(new ArrayList<>(exportFields));
		this.title = title;
		this.dto = dto;
		this.dtc = dtc;
	}

	/**
	 * @return titre de cet objet/liste
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Liste des informations sur les Fields à exporter
	 */
	public List<ExportField> getExportFields() {
		return exportFields;
	}

	/**
	 * @return Données sous forme d'un DTO, ceci est un cast donc il y a levé d'une assertion si ce n'est pas un DTO
	 */
	public DtObject getDtObject() {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		return dto;
	}

	/**
	 * @return Données sous forme d'une DTC, ceci est un cast donc il y a levé d'une assertion si ce n'est pas une DTC
	 */
	public DtList<?> getDtList() {
		Assertion.checkNotNull(dtc);
		//---------------------------------------------------------------------
		return dtc;
	}

	/**
	 * Le paramètre contient soit un DTO, soit une DTC.
	 * @return boolean true, si il contient un DTO
	 */
	public boolean hasDtObject() {
		return dto != null;
	}
}

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

import io.vertigo.core.lang.Builder;
import io.vertigo.core.lang.MessageText;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;

/**
 * Parametre d'export pour les données de type DT.
 * La particularité est que l'on fournit la liste des colonnes du DT a exporter,
 * avec éventuellement des paramètres d'affichage particulier pour une colonne.
 * @author npiedeloup
 */
public interface ExportDtParametersBuilder extends  Builder<ExportDtParameters> {
	/**
	 * Ajoute un champs du Dt dans l'export, le label de la colonne sera celui indiqué dans le DT pour ce champs.
	 * @param exportfield ajout d'un champs du Dt à exporter
	 */
	ExportDtParametersBuilder withField(final DtField exportfield);

	/**
	 * @param exportfield ajout d'un champs du Dt à exporter
	 * @param label nom spécifique à utiliser dans l'export, null si l'on souhaite utiliser celui indiqué dans le DT pour ce champs
	 */
	ExportDtParametersBuilder withField(final DtField exportfield, final MessageText label);

	/**
	 * Ajoute un champs du Dt dans l'export, le label de la colonne sera celui indiqué dans le DT pour ce champs.
	 * @param exportfield ajout d'un champs du Dt à exporter
	 * @param list Liste des éléments dénormés
	 * @param displayfield Field du libellé à utiliser.
	 */
	ExportDtParametersBuilder withField(final DtField exportfield, final DtList<?> list, final DtField displayfield);

	/**
	 * @param exportfield ajout d'un champs du Dt à exporter
	 * @param list Liste des éléments dénormés
	 * @param displayfield Field du libellé à utiliser.
	 * @param label nom spécifique à utiliser dans l'export, null si l'on souhaite utiliser celui indiqué dans le DT pour ce champs
	 */
	ExportDtParametersBuilder withField(final DtField exportfield, final DtList<?> list, final DtField displayfield, final MessageText label);

}

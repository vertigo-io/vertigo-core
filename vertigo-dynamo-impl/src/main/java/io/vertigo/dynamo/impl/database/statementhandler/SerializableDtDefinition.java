/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.database.statementhandler;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DomainBuilder;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinitionBuilder;
import io.vertigo.lang.Assertion;

/**
 * DtDefinition serializable.
 * Permet de serialiser une DT qui par nature n'est pas sérialisable.
 * @author pchretien
 */
final class SerializableDtDefinition implements Serializable {
	private static final String DT_DYNAMIC = "DT_DYNAMIC_DTO";
	//Map des domaines correspondants aux types primitifs
	private static final Map<DataType, Domain> DOMAIN_MAP = createDomainMap();

	private static final long serialVersionUID = -423652372994923330L;
	private final SerializableDtField[] fields;
	private transient DtDefinition dtDefinition;

	/**
	 * @param fields Fields
	 */
	SerializableDtDefinition(final SerializableDtField[] fields) {
		Assertion.checkNotNull(fields);
		//-----
		this.fields = fields;
	}

	/**
	 * @return DtDefinition
	 */
	public synchronized DtDefinition getDtDefinition() {
		//synchronizer, car lasy loading
		if (dtDefinition == null) {
			final DtDefinitionBuilder dtDefinitionBuilder = new DtDefinitionBuilder(DT_DYNAMIC)
					.withDynamic(true);

			for (final SerializableDtField field : fields) {
				//On considére le champ nullable et non persistent
				dtDefinitionBuilder.addDataField(field.getName(), field.getLabel(), getDomain(field.getDataType()), false, false, false, false);
			}
			dtDefinition = dtDefinitionBuilder.build();
		}
		return dtDefinition;
	}

	private static Map<DataType, Domain> createDomainMap() {
		final DataType[] dataTypes = DataType.values();
		final Map<DataType, Domain> map = new EnumMap<>(DataType.class);
		//Initialisation de la map.
		for (final DataType dataType : dataTypes) {
			final Domain domain = new DomainBuilder("DO_DYN", dataType).build();
			map.put(dataType, domain);
		}
		return map;
	}

	private static Domain getDomain(final DataType dataType) {
		final Domain domain = DOMAIN_MAP.get(dataType);
		Assertion.checkNotNull(domain);
		return domain;
	}
}

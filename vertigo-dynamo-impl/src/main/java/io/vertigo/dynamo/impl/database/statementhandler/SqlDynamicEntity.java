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

import java.util.HashMap;
import java.util.Map;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.Dynamic;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

/**
 * DTO dynamique.
 * Fabrication dynamique d'un DTO par sa définition.
 *
 * @author  pchretien
 */
final class SqlDynamicEntity implements Entity, Dynamic {
	private static final long serialVersionUID = 1L;

	/** Définition Sérializable de l'objet. */
	private final SerializableDtDefinition serializableDefinition;
	private final Map<String, Object> values = new HashMap<>();

	/**
	 * Constructeur
	 * @param serializableDefinition DT serializable
	 */
	SqlDynamicEntity(final SerializableDtDefinition serializableDefinition) {
		Assertion.checkNotNull(serializableDefinition);
		//-----
		this.serializableDefinition = serializableDefinition;
	}

	/** {@inheritDoc} */
	@Override
	public URI getURI() {
		return DtObjectUtil.createURI(this);
	}

	/** {@inheritDoc} */
	@Override
	public DtDefinition getDefinition() {
		return serializableDefinition.getDtDefinition();
	}

	/** {@inheritDoc} */
	@Override
	public void setValue(final DtField dtField, final Object value) {
		values.put(dtField.getName(), value);
	}

	/** {@inheritDoc} */
	@Override
	public Object getValue(final DtField dtField) {
		return values.get(dtField.getName());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}

}

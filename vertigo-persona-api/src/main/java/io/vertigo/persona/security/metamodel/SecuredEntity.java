/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.persona.security.metamodel;

import java.util.List;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;

/**
 * Une permission est l'association d'une opération et d'une ressource.
 *
 * @author jgarnier
 */
@DefinitionPrefix("SEC_")
public final class SecuredEntity implements Definition {
	private final String name;
	private final DtDefinition entityDefinition;
	private final List<DtField> securityFields;
	private final List<SecurityAxe> advancedAxes;
	private final List<Permission2> operations;

	/**
	 * Construct an instance of SecurityEntity.
	 *
	 * @param entityDefinition Entity sécurisé.
	 * @param securityFields fields simple de sécurité.
	 * @param advancedAxes axes avancés de sécurité.
	 * @param operations opérations attribuées.
	 */
	public SecuredEntity(final DtDefinition entityDefinition, final List<DtField> securityFields, final List<SecurityAxe> advancedAxes, final List<Permission2> operations) {
		name = "SEC_" + entityDefinition.getName();
		this.entityDefinition = entityDefinition;
		this.securityFields = securityFields;
		this.advancedAxes = advancedAxes;
		this.operations = operations;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Give the value of entity.
	 *
	 * @return the value of entity.
	 */
	public DtDefinition getEntity() {
		return entityDefinition;
	}

	/**
	 * Give the list of security fieldNames.
	 *
	 * @return the list of security fieldNames.
	 */
	public List<DtField> getSecurityFields() {
		return securityFields;
	}

	/**
	 * Give the value of axes.
	 *
	 * @return the value of axes.
	 */
	public List<SecurityAxe> getSecurityAxes() {
		return advancedAxes;
	}

	/**
	 * Give the value of operations.
	 *
	 * @return the value of operations.
	 */
	public List<Permission2> getOperations() {
		return operations;
	}
}

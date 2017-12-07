/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.account.account.model;

import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * User.
 */
public final class UserGroup implements KeyConcept {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String grpId;
	private String name;
	private String otherProperty;

	/** {@inheritDoc} */
	@Override
	public URI<UserGroup> getURI() {
		return DtObjectUtil.createURI(this);
	}

	@Field(domain = "DO_CODE", type = "ID", required = true, label = "Id")
	public final String getGrpId() {
		return grpId;
	}

	public final void setGrpId(final String grpId) {
		this.grpId = grpId;
	}

	@Field(domain = "DO_LABEL", required = true, label = "Name")
	public final String getName() {
		return name;
	}

	public final void setName(final String name) {
		this.name = name;
	}

	@Field(domain = "DO_LABEL", required = true, label = "otherProperty")
	public final String getOtherProperty() {
		return otherProperty;
	}

	public final void setOtherProperty(final String otherProperty) {
		this.otherProperty = otherProperty;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}

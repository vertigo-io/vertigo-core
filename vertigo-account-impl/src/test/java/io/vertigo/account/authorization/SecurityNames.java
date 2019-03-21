/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.account.authorization;

import io.vertigo.account.authorization.metamodel.AuthorizationName;
import io.vertigo.account.authorization.metamodel.OperationName;
import io.vertigo.account.authorization.model.Record;

public final class SecurityNames {

	/**
	 * Enumération des Authorizations globales.
	 */
	public enum GlobalAuthorizations implements AuthorizationName {
		ATZ_ADMUSR,
		ATZ_ADMPRO,
		ATZ_ADMAPP
	}

	/**
	 * Enumération des Authorizations globales.
	 */
	public enum RecordAuthorizations implements AuthorizationName {
		ATZ_RECORD$READ,
		ATZ_RECORD$READ2,
		ATZ_RECORD$READ_HP,
		ATZ_RECORD$DELETE,
		ATZ_RECORD$NOTIFY,
		ATZ_RECORD$CREATE,
		ATZ_RECORD$WRITE;
	}

	/**
	 * Enumération des opérations de Dossier.
	 */
	public enum RecordOperations implements OperationName<Record> {
		READ,
		READ2,
		READ_HP,
		WRITE,
		CREATE,
		DELETE,
		NOTIFY,
	}
}

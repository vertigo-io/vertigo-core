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
package io.vertigo.account.account;

import java.util.Optional;
import java.util.Set;

import io.vertigo.core.component.Manager;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.VFile;

/**
 * Centralized management of accounts.
 *
 * @author npiedeloup
 */
public interface AccountManager extends Manager {

	/**
	 * Gets the default photo of any account.
	 * This default photo will be used if an account does not have one
	 * @return the photo as a file
	 */
	VFile getDefaultPhoto();

	/**
	 * @param accountURI the account defined by its URI
	 * @return the account
	 */
	Account getAccount(URI<Account> accountURI);

	/**
	 * Gets the group defined by an URI.
	 * @param groupURI the group URI
	 * @return the group
	 */
	AccountGroup getGroup(URI<AccountGroup> groupURI);

	/**
	 * Lists the accounts for a defined group.
	 * @param groupURI the group URI
	 * @return the list of acccounts.
	 */
	Set<URI<Account>> getAccountURIs(URI<AccountGroup> groupURI);

	/**
	 * @param accountURI the account defined by its URI
	 * @return set of groups of this account
	 */
	Set<URI<AccountGroup>> getGroupURIs(URI<Account> accountURI);

	/**
	 * Gets the photo of an account defined by its URI.
	 *
	 * @param accountURI the account defined by its URI
	 * @return the photo as a file
	 */
	Optional<VFile> getPhoto(URI<Account> accountURI);

	/**
	 * Gets an newly authentified account by his authToken.
	 * @param userAuthToken user authToken
	 * @return the logged account
	 */
	Optional<Account> getAccountByAuthToken(String userAuthToken);
}

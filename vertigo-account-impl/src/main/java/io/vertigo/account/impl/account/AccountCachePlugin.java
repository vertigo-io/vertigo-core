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
package io.vertigo.account.impl.account;

import java.util.Optional;
import java.util.Set;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.core.component.Plugin;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.VFile;

/**
 * @author pchretien
 */
public interface AccountCachePlugin extends Plugin {

	/**
	 * @param accountURI the account defined by its URI
	 * @return the account
	 */
	Optional<Account> getAccount(URI<Account> accountURI);

	/**
	 * @param accountURI the account defined by its URI
	 * @return Set of groups of this account
	 */
	Set<URI<AccountGroup>> getGroupURIs(URI<Account> accountURI);

	/**
	 * Gets the group defined by an URI.
	 * @param groupURI the group URI
	 * @return the group
	 */
	Optional<AccountGroup> getGroup(URI<AccountGroup> groupURI);

	/**
	 * Lists the accounts for a defined group.
	 * @param groupURI the group URI
	 * @return the list of acccounts.
	 */
	Set<URI<Account>> getAccountURIs(URI<AccountGroup> groupURI);

	/**
	 * Gets the photo of an account defined by its URI.
	 *
	 * @param accountURI the account defined by its URI
	 * @return the photo as a file
	 */
	Optional<VFile> getPhoto(URI<Account> accountURI);

	/**
	 * Saves an account.
	 * Caution : all the accounts must have an id.
	 * @param account account
	 */
	void putAccount(Account account);

	/**
	 * Saves a group.
	 * @param group the group
	 */
	void putGroup(AccountGroup group);

	/**
	 * Attaches an account to a group.
	 * @param accountURI the account defined by its URI
	 * @param groupURI the group
	 */
	void attach(URI<Account> accountURI, Set<URI<AccountGroup>> groupURI);

	/**
	 * Attaches an account to a group.
	 * @param accountsURI the accounts defined by their URI
	 * @param groupURI the group
	 */
	void attach(Set<URI<Account>> accountsURI, URI<AccountGroup> groupURI);

	/**
	 * Reset:
	 * - All the accounts
	 * - All the groups
	 * - All the links accounts-group
	 * - All the Photos
	 */
	void reset();

	/**
	 * Defines a photo to an account.
	 *
	 * @param accountURI the account defined by its URI
	 * @param photo the photo
	 */
	void setPhoto(URI<Account> accountURI, VFile photo);

	/**
	 * Get an newly authentify user by his authToken.
	 * @param userAuthToken user authToken
	 * @return Logged account
	 */
	Optional<Account> getAccountByAuthToken(String userAuthToken);

}

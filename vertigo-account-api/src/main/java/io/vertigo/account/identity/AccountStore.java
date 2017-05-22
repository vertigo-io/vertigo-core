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
package io.vertigo.account.identity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.VFile;

/**
 * This class defines the storage of accounts
 * @author pchretien
 */
public interface AccountStore {

	/**
	 * @return the number of accounts
	 */
	long getAccountsCount();

	/**
	 * @param accountURI the account defined by its URI
	 * @return the account
	 */
	Account getAccount(URI<Account> accountURI);

	/**
	 * @param accountURI the account defined by its URI
	 * @return Set of groups of this account
	 */
	Set<URI<AccountGroup>> getGroupURIs(URI<Account> accountURI);

	/**
	 * @return the number of groups.
	 */
	long getGroupsCount();

	/**
	 * Lists all the groups.
	 * @return all the groups.
	 */
	Collection<AccountGroup> getAllGroups();

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
	 * Gets the photo of an account defined by its URI.
	 *
	 * @param accountURI the account defined by its URI
	 * @return the photo as a file
	 */
	Optional<VFile> getPhoto(URI<Account> accountURI);

	/**
	 * Saves a collection of accounts.
	 * Caution : all the accounts must have an id.
	 * @param accounts the list of accounts
	 */
	void saveAccounts(List<Account> accounts);

	/**
	 * Saves a group.
	 * @param group the group
	 */
	void saveGroup(AccountGroup group);

	/**
	 * Attaches an account to a group.
	 * @param accountURI the account defined by its URI
	 * @param groupURI the group
	 */
	void attach(URI<Account> accountURI, URI<AccountGroup> groupURI);

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

}

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
package io.vertigo.account.plugins.identity.memory;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import io.vertigo.account.identity.Account;
import io.vertigo.account.identity.AccountGroup;
import io.vertigo.account.impl.identity.IdentityRealmPlugin;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.VFile;

/**
 * @author npiedeloup
 */
public final class MemoryIdentityRealmPlugin implements IdentityRealmPlugin {

	/** {@inheritDoc} */
	@Override
	public Account getAccountByAuthToken(final String userAuthToken) {
		return null; //TODO
	}

	@Override
	public long getAccountsCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<Account> getAllAccounts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<URI<AccountGroup>> getGroupURIs(final URI<Account> accountURI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getGroupsCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<AccountGroup> getAllGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccountGroup getGroup(final URI<AccountGroup> groupURI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<URI<Account>> getAccountURIs(final URI<AccountGroup> groupURI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<VFile> getPhoto(final URI<Account> accountURI) {
		// TODO Auto-generated method stub
		return null;
	}

}

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
package io.vertigo.account.plugins.account.cache.memory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.impl.account.AccountCachePlugin;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;

/**
 * @author pchretien
 */
public final class MemoryAccountCachePlugin implements AccountCachePlugin {
	private final Map<UID<Account>, Account> accountByURI = new HashMap<>();
	private final Map<String, UID<Account>> accountURIByAuthToken = new HashMap<>();
	private final Map<UID<AccountGroup>, AccountGroup> groupByURI = new HashMap<>();
	//---
	private final Map<UID<Account>, Set<UID<AccountGroup>>> groupByAccountURI = new HashMap<>();
	private final Map<UID<AccountGroup>, Set<UID<Account>>> accountByGroupURI = new HashMap<>();
	//---
	private final Map<UID<Account>, VFile> photoByAccountURI = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public synchronized Optional<Account> getAccount(final UID<Account> accountURI) {
		Assertion.checkNotNull(accountURI);
		//-----
		return Optional.ofNullable(accountByURI.get(accountURI));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void putAccount(final Account account) {
		Assertion.checkNotNull(account);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(account);
		final UID<Account> uri = new UID<>(dtDefinition, account.getId());
		//----
		final Object old = accountByURI.put(uri, account);
		if (old == null) {
			groupByAccountURI.put(uri, new HashSet<UID<AccountGroup>>());
			accountURIByAuthToken.put(account.getAuthToken(), uri);
		}
	}

	//-----
	/** {@inheritDoc} */
	@Override
	public synchronized Optional<AccountGroup> getGroup(final UID<AccountGroup> groupURI) {
		Assertion.checkNotNull(groupURI);
		//-----
		return Optional.ofNullable(groupByURI.get(groupURI));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void putGroup(final AccountGroup group) {
		Assertion.checkNotNull(group);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(group);
		final UID<AccountGroup> uri = new UID<>(dtDefinition, group.getId());
		//----
		Assertion.checkArgument(!groupByURI.containsKey(uri), "this group is already registered, you can't create it");
		//-----
		accountByGroupURI.put(uri, new HashSet<UID<Account>>());
		groupByURI.put(uri, group);
	}

	//-----
	/** {@inheritDoc} */
	@Override
	public synchronized void attach(final Set<UID<Account>> accountsURI, final UID<AccountGroup> groupURI) {
		Assertion.checkNotNull(accountsURI);
		Assertion.checkNotNull(groupURI);
		//-----
		accountsURI.forEach(accountURI -> this.attach(accountURI, groupURI));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void attach(final UID<Account> accountURI, final Set<UID<AccountGroup>> groupURIs) {
		//-----
		groupURIs.forEach(groupURI -> this.attach(accountURI, groupURI));
	}

	private synchronized void attach(final UID<Account> accountURI, final UID<AccountGroup> groupURI) {
		Assertion.checkNotNull(accountURI);
		Assertion.checkNotNull(groupURI);
		//-----
		final Set<UID<AccountGroup>> groupURIs = groupByAccountURI.get(accountURI);
		Assertion.checkNotNull(groupURIs, "account must be create before this operation");
		groupURIs.add(groupURI);
		//-----
		final Set<UID<Account>> accountURIs = accountByGroupURI.get(groupURI);
		Assertion.checkNotNull(accountURIs, "group must be create before this operation");
		accountURIs.add(accountURI);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Set<UID<AccountGroup>> getGroupURIs(final UID<Account> accountURI) {
		Assertion.checkNotNull(accountURI);
		//-----
		final Set<UID<AccountGroup>> groupURIs = groupByAccountURI.get(accountURI);
		if (groupURIs == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(groupURIs);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Set<UID<Account>> getAccountURIs(final UID<AccountGroup> groupURI) {
		Assertion.checkNotNull(groupURI);
		//-----
		final Set<UID<Account>> accountURIs = accountByGroupURI.get(groupURI);
		if (accountURIs == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(accountURIs);
	}

	/** {@inheritDoc} */
	@Override
	public void setPhoto(final UID<Account> accountURI, final VFile photo) {
		Assertion.checkNotNull(accountURI);
		Assertion.checkNotNull(photo);
		//-----
		photoByAccountURI.put(accountURI, photo);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final UID<Account> accountURI) {
		Assertion.checkNotNull(accountURI);
		//-----
		return Optional.ofNullable(photoByAccountURI.get(accountURI));
	}

	/** {@inheritDoc} */
	@Override
	public void reset() {
		photoByAccountURI.clear();
		accountByGroupURI.clear();
		accountByURI.clear();
		groupByAccountURI.clear();
		groupByURI.clear();
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		final UID<Account> accountURI = accountURIByAuthToken.get(userAuthToken);
		if (accountURI != null) {
			return getAccount(accountURI);
		}
		return Optional.empty();
	}

}

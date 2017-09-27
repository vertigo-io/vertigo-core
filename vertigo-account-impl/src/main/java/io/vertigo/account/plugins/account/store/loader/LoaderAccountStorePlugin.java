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
package io.vertigo.account.plugins.account.store.loader;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.impl.account.AccountStorePlugin;
import io.vertigo.app.Home;
import io.vertigo.core.component.Activeable;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;

/**
 * @author pchretien
 */
public final class LoaderAccountStorePlugin implements AccountStorePlugin, Activeable {

	private final String accountLoaderName;
	private final Optional<String> groupLoaderName;
	private AccountLoader accountLoader;
	private Optional<GroupLoader> groupLoader;

	/**
	 * @param accountLoaderName Custom AccountLoader component name
	 * @param groupLoaderName Custom GroupLoader component name
	 */
	@Inject
	public LoaderAccountStorePlugin(
			@Named("accountLoaderName") final String accountLoaderName,
			@Named("groupLoaderName") final Optional<String> groupLoaderName) {
		Assertion.checkArgNotEmpty(accountLoaderName);
		Assertion.checkNotNull(groupLoaderName);

		this.accountLoaderName = accountLoaderName;
		this.groupLoaderName = groupLoaderName;
	}

	/** {@inheritDoc} */
	@Override
	public final void start() {
		accountLoader = Home.getApp().getComponentSpace().resolve(accountLoaderName, AccountLoader.class);
		if (groupLoaderName.isPresent()) {
			groupLoader = Optional.of(Home.getApp().getComponentSpace().resolve(groupLoaderName.get(), GroupLoader.class));
		} else {
			groupLoader = Optional.empty();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//rien
	}

	/** {@inheritDoc} */
	@Override
	public long getAccountsCount() {
		return accountLoader.getAccountsCount();
	}

	/** {@inheritDoc} */
	@Override
	public long getGroupsCount() {
		return getGroupLoader().getGroupsCount();
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Account getAccount(final URI<Account> accountURI) {
		return accountLoader.getAccount(accountURI);
	}

	//-----
	/** {@inheritDoc} */
	@Override
	public synchronized AccountGroup getGroup(final URI<AccountGroup> groupURI) {
		return getGroupLoader().getGroup(groupURI);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Set<URI<AccountGroup>> getGroupURIs(final URI<Account> accountURI) {
		return getGroupLoader().getGroupURIs(accountURI);
	}

	private GroupLoader getGroupLoader() {
		return groupLoader.orElseThrow(() -> new UnsupportedOperationException("No GroupLoader was defined, Groups operations are not supported"));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Set<URI<Account>> getAccountURIs(final URI<AccountGroup> groupURI) {
		return getGroupLoader().getAccountURIs(groupURI);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final URI<Account> accountURI) {
		return accountLoader.getPhoto(accountURI);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		return accountLoader.getAccountByAuthToken(userAuthToken);
	}

}

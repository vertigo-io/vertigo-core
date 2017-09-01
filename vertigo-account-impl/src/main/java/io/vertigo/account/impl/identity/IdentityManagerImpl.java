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
package io.vertigo.account.impl.identity;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.account.identity.Account;
import io.vertigo.account.identity.AccountGroup;
import io.vertigo.account.identity.IdentityManager;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;

/**
 * @author pchretien, npiedeloup
 */
public final class IdentityManagerImpl implements IdentityManager {
	private final AccountStorePlugin accountStorePlugin; //accès local aux users
	private final Optional<AccountCachePlugin> accountCachePlugin;
	private final Optional<IdentityProviderPlugin> identityProviderPlugin;
	private final boolean defaultToIdentityProvider;
	private final VFile defaultPhoto;

	/**
	 * Constructor.
	 * @param accountStorePlugin the account store plugin
	 * @param accountCachePlugin the account cache plugin
	 * @param identityProviderPlugin the identity provider plugin
	 * @param defaultToIdentityProvider If we default to IdentityProvider if data not in store
	 * @param fileManager the file manager
	 */
	@Inject
	public IdentityManagerImpl(
			final AccountStorePlugin accountStorePlugin,
			final Optional<AccountCachePlugin> accountCachePlugin,
			final Optional<IdentityProviderPlugin> identityProviderPlugin,
			@Named("defaultToIdentityProvider") final Optional<Boolean> defaultToIdentityProvider,
			final FileManager fileManager) {
		Assertion.checkNotNull(accountStorePlugin);
		Assertion.checkNotNull(accountCachePlugin);
		Assertion.checkNotNull(identityProviderPlugin);
		Assertion.checkNotNull(fileManager);
		Assertion.when(defaultToIdentityProvider.isPresent() && defaultToIdentityProvider.get())
				.check(identityProviderPlugin::isPresent, "When defaultToIdentityProvider is set, you must provide a IdentityProviderPlugin");
		Assertion.checkArgument(!defaultToIdentityProvider.isPresent(), "TODO : defaultToIdentityProvider not implemented yet");

		//-----
		this.accountStorePlugin = accountStorePlugin;
		this.accountCachePlugin = accountCachePlugin;
		this.identityProviderPlugin = identityProviderPlugin;
		this.defaultToIdentityProvider = defaultToIdentityProvider.orElse(false);
		defaultPhoto = fileManager.createFile(
				"defaultPhoto.png",
				"image/png",
				IdentityManagerImpl.class.getResource("defaultPhoto.png"));
	}

	/** {@inheritDoc} */
	@Override
	public VFile getDefaultPhoto() {
		return defaultPhoto;
	}

	//------------------//
	//-- AccountStore --//
	//------------------//
	/** {@inheritDoc} */
	@Override
	public Account getAccount(final URI<Account> accountURI) {
		return loadWithCache(accountURI,
				() -> accountCachePlugin.get()::getAccount,
				accountStorePlugin::getAccount,
				() -> accountCachePlugin.get()::putAccount);
	}

	/** {@inheritDoc} */
	@Override
	public AccountGroup getGroup(final URI<AccountGroup> groupURI) {
		return loadWithCache(groupURI,
				() -> accountCachePlugin.get()::getGroup,
				accountStorePlugin::getGroup,
				() -> accountCachePlugin.get()::putGroup);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final URI<Account> accountURI) {
		return loadWithCacheOptionalValue(accountURI,
				() -> accountCachePlugin.get()::getPhoto,
				accountStorePlugin::getPhoto,
				() -> photo -> accountCachePlugin.get().setPhoto(accountURI, photo));
	}

	/** {@inheritDoc} */
	@Override
	public Set<URI<Account>> getAccountURIs(final URI<AccountGroup> groupURI) {
		return loadWithCacheSetValue(groupURI,
				() -> accountCachePlugin.get()::getAccountURIs,
				accountStorePlugin::getAccountURIs,
				() -> accountsURI -> accountCachePlugin.get().attach(accountsURI, groupURI));
	}

	/** {@inheritDoc} */
	@Override
	public Set<URI<AccountGroup>> getGroupURIs(final URI<Account> accountURI) {
		return loadWithCacheSetValue(accountURI,
				() -> accountCachePlugin.get()::getGroupURIs,
				accountStorePlugin::getGroupURIs,
				() -> groupsURI -> accountCachePlugin.get().attach(accountURI, groupsURI));
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		return loadWithCacheOptionalValue(userAuthToken,
				() -> accountCachePlugin.get()::getAccountByAuthToken,
				accountStorePlugin::getAccountByAuthToken,
				() -> accountCachePlugin.get()::putAccount);
	}

	private <O extends Object, U extends Object> Optional<O> loadWithCacheOptionalValue(final U uri, final Supplier<Function<U, Optional<O>>> cacheSupplier, final Function<U, Optional<O>> storeSupplier, final Supplier<Consumer<O>> cacheRegister) {
		if (accountCachePlugin.isPresent()) {
			final Optional<O> resultOpt = cacheSupplier.get().apply(uri);
			if (!resultOpt.isPresent()) {
				final Optional<O> result = storeSupplier.apply(uri);
				if (result.isPresent()) {
					cacheRegister.get().accept(result.get());
				}
				return result;
			}
		}
		return storeSupplier.apply(uri);

	}

	private <O extends Object, U extends Object> Set<O> loadWithCacheSetValue(final U uri, final Supplier<Function<U, Set<O>>> cacheSupplier, final Function<U, Set<O>> storeSupplier, final Supplier<Consumer<Set<O>>> cacheRegister) {
		if (accountCachePlugin.isPresent()) {
			final Set<O> resultOpt = cacheSupplier.get().apply(uri);
			if (!resultOpt.isEmpty()) {
				final Set<O> result = storeSupplier.apply(uri);
				if (!result.isEmpty()) {
					cacheRegister.get().accept(result);
				}
				return result;
			}
		}
		return storeSupplier.apply(uri);

	}

	private <O extends Object, U extends Object> O loadWithCache(final U uri, final Supplier<Function<U, Optional<O>>> cacheSupplier, final Function<U, O> storeSupplier, final Supplier<Consumer<O>> cacheRegister) {
		if (accountCachePlugin.isPresent()) {
			final Optional<O> resultOpt = cacheSupplier.get().apply(uri);
			if (!resultOpt.isPresent()) {
				final O result = storeSupplier.apply(uri);
				cacheRegister.get().accept(result);
				return result;
			}
		}
		return storeSupplier.apply(uri);
	}

	/*public long provisionUsersFromIdentityProviders() {
		if (identityProviderPlugin.isPresent()) {
			final List<Account> accounts = identityProviderPlugin.get().getAllUsers()
					.stream()
					.map(this::convertUserToAccount)
					.collect(Collectors.toList());
			accountStorePlugin.saveAccounts(accounts);
			return accounts.size();
		}
		return 0;
	}*/

}

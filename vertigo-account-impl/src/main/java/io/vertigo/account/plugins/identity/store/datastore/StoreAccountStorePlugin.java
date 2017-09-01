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
package io.vertigo.account.plugins.identity.store.datastore;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.account.identity.Account;
import io.vertigo.account.identity.AccountGroup;
import io.vertigo.account.impl.identity.AccountStorePlugin;
import io.vertigo.account.plugins.identity.store.AbstractAccountStorePlugin;
import io.vertigo.app.Home;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;

/**
 * Source of identity.
 * @author npiedeloup
 */
public final class StoreAccountStorePlugin extends AbstractAccountStorePlugin implements AccountStorePlugin {

	private final StoreManager storeManager;

	private final String groupIdentityEntity;
	private final String userAuthField;
	private DtDefinition userGroupDtDefinition;

	/**
	 * Constructor.
	 * @param userIdentityEntity Entity name of userIdentityEntity
	 * @param userAuthField FieldName use to find user by it's authToken
	 * @param storeManager Store Manager
	 */
	@Inject
	public StoreAccountStorePlugin(
			@Named("userIdentityEntity") final String userIdentityEntity,
			@Named("groupIdentityEntity") final String groupIdentityEntity,
			@Named("userAuthField") final String userAuthField,
			@Named("userToAccountMapping") final String userToAccountMappingStr,
			final StoreManager storeManager) {
		super(userIdentityEntity, userToAccountMappingStr);
		Assertion.checkArgNotEmpty(userIdentityEntity);
		Assertion.checkArgNotEmpty(userAuthField);
		Assertion.checkNotNull(storeManager);
		this.groupIdentityEntity = groupIdentityEntity;
		this.userAuthField = userAuthField;
		this.storeManager = storeManager;
	}

	@Override
	protected void postStart() {
		userGroupDtDefinition = Home.getApp().getDefinitionSpace().resolve(groupIdentityEntity, DtDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public long getAccountsCount() {
		return storeManager.getDataStore().count(getUserDtDefinition());
	}

	/** {@inheritDoc} */
	@Override
	public Account getAccount(final URI<Account> accountURI) {
		final URI<Entity> userURI = new URI(getUserDtDefinition(), accountURI.getId());
		final Entity userEntity = storeManager.getDataStore().readOne(userURI);
		return userToAccount(userEntity);
	}

	@Override
	public Set<URI<AccountGroup>> getGroupURIs(final URI<Account> accountURI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getGroupsCount() {
		return storeManager.getDataStore().count(userGroupDtDefinition);
	}

	@Override
	public AccountGroup getGroup(final URI<AccountGroup> accountGroupURI) {
		throw new UnsupportedOperationException("Not implemented yet");
		/*final URI<Entity> groupURI = new URI(userGroupDtDefinition, accountGroupURI.getId());
		final Entity groupEntity = storeManager.getDataStore().readOne(groupURI);
		return groupToAccount(groupEntity);*/
	}

	@Override
	public Set<URI<Account>> getAccountURIs(final URI<AccountGroup> groupURI) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<VFile> getPhoto(final URI<Account> accountURI) {
		return Optional.empty(); //TODO
	}

	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		final Criteria<Entity> criteriaByAuthToken = Criterions.isEqualTo(() -> userAuthField, userAuthToken);
		final DtList<Entity> results = storeManager.getDataStore().find(getUserDtDefinition(), criteriaByAuthToken);
		Assertion.checkState(results.size() <= 1, "Too many matching for authToken {0}", userAuthToken);
		if (!results.isEmpty()) {
			return Optional.of(userToAccount(results.get(0)));
		}
		return Optional.empty();
	}
}

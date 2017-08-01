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
package io.vertigo.account.plugins.identity.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.account.identity.Account;
import io.vertigo.account.impl.identity.IdentityRealmPlugin;
import io.vertigo.app.Home;
import io.vertigo.core.component.Activeable;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;

/**
 * Source of identity.
 * @author npiedeloup
 */
public final class StoreIdentityRealmPlugin implements IdentityRealmPlugin, Activeable {

	private final StoreManager storeManager;

	private final String userIdentityEntity;
	private final String userAuthField;
	private final Map<AccountProperty, String> userAccountFieldsMapping; //Account properties to ldapAttribute
	private DtDefinition userIdentityDefinition;

	private enum AccountProperty {
		id, displayName, email, authToken, photo
	}

	/**
	 * Constructor.
	 * @param userIdentityEntity Entity name of userIdentityEntity
	 * @param userAuthField FieldName use to find user by it's authToken
	 * @param userAccountFieldsMappingStr Mapping from LDAP to Account
	 * @param storeManager Store Manager
	 */
	@Inject
	public StoreIdentityRealmPlugin(
			@Named("userIdentityEntity") final String userIdentityEntity,
			@Named("userAuthField") final String userAuthField,
			@Named("userAccountFieldsMapping") final String userAccountFieldsMappingStr,
			final StoreManager storeManager) {
		Assertion.checkArgNotEmpty(userIdentityEntity);
		Assertion.checkArgNotEmpty(userAuthField);
		Assertion.checkArgNotEmpty(userAccountFieldsMappingStr);
		Assertion.checkNotNull(storeManager);
		this.userIdentityEntity = userIdentityEntity;
		this.userAuthField = userAuthField;
		userAccountFieldsMapping = parseAccountFieldsMapping(userAccountFieldsMappingStr, AccountProperty.class);
		Assertion.checkArgNotEmpty(userAccountFieldsMapping.get(AccountProperty.id), "userAccountFieldsMapping must declare mapping for accountProperty {0}" + AccountProperty.id);
		Assertion.checkArgNotEmpty(userAccountFieldsMapping.get(AccountProperty.displayName), "userAccountFieldsMapping must declare mapping for accountProperty {0}" + AccountProperty.displayName);
		this.storeManager = storeManager;
	}

	/** {@inheritDoc} */
	@Override
	public Account getAccountByAuthToken(final String userAuthToken) {
		final Criteria<Entity> criteriaByAuthToken = Criterions.isEqualTo(() -> userAuthField, userAuthToken);
		final DtList<Entity> results = storeManager.getDataStore().find(userIdentityDefinition, criteriaByAuthToken);
		Assertion.checkState(results.size() <= 1, "Too many matching for authToken {0}", userAuthToken);
		Assertion.checkState(!results.isEmpty(), "No user found for this authToken {0}", userAuthToken);

		return parseAccount(results.get(0));
	}

	/** {@inheritDoc} */
	@Override
	public long getAccountsCount() {
		return storeManager.getDataStore().count(userIdentityDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public Collection<Account> getAllAccounts() {
		final DtList<Entity> results = storeManager.getDataStore().find(userIdentityDefinition, Criterions.alwaysTrue());
		return results.stream()
				.map(this::parseAccount)
				.collect(Collectors.toList());
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final URI<Account> accountURI) {
		return Optional.empty(); //TODO
	}

	private static <E extends Enum<E>> Map<E, String> parseAccountFieldsMapping(final String userFieldsMapping, final Class<E> enumClass) {
		final Map<E, String> accountAttributeMapping = new HashMap<>();
		for (final String mapping : userFieldsMapping.split("\\s*,\\s*")) {
			final String[] splitedMapping = mapping.split("\\s*:\\s*");
			Assertion.checkArgument(splitedMapping.length == 2, "Mapping should respect the pattern : StoreAttr1:AccountAttr1, StoreAttr2:AccountAttr2, ... (check : {0})", userFieldsMapping);
			//It's reverse compared to config String : we keep a map of key:accountProperty -> value:storeAttribute
			accountAttributeMapping.put(Enum.valueOf(enumClass, splitedMapping[1]), splitedMapping[2]);
		}
		return accountAttributeMapping;
	}

	private Account parseAccount(final Entity userIdentity) {
		final String accountId = parseAttribute(String.class, AccountProperty.id, userIdentity);
		final String authToken = parseAttribute(String.class, AccountProperty.authToken, userIdentity);
		final String displayName = parseAttribute(String.class, AccountProperty.displayName, userIdentity);
		final String email = parseOptionalAttribute(String.class, AccountProperty.email, userIdentity);
		return Account.builder(accountId)
				.withAuthToken(authToken)
				.withDisplayName(displayName)
				.withEmail(email)
				.build();
	}

	private <O> O parseAttribute(final Class<O> valueClass, final AccountProperty accountProperty, final DtObject dtObject) {
		final String fieldName = userAccountFieldsMapping.get(accountProperty);
		return valueClass.cast(userIdentityDefinition.getField(fieldName).getDataAccessor().getValue(dtObject));
	}

	private <O> O parseOptionalAttribute(final Class<O> valueClass, final AccountProperty accountProperty, final DtObject dtObject) {
		final String fieldName = userAccountFieldsMapping.get(accountProperty);
		if (fieldName != null) {
			return valueClass.cast(userIdentityDefinition.getField(fieldName).getDataAccessor().getValue(dtObject));
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		userIdentityDefinition = Home.getApp().getDefinitionSpace().resolve(userIdentityEntity, DtDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//nothing
	}

}

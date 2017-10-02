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
package io.vertigo.account.plugins.account.store.datastore;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.impl.account.AccountMapperHelper;
import io.vertigo.account.impl.account.AccountStorePlugin;
import io.vertigo.account.plugins.account.store.AbstractAccountStorePlugin;
import io.vertigo.app.Home;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
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
	private final String groupToGroupAccountMappingStr;
	private AssociationDefinition associationUserGroup;
	private String associationGroupRoleName;
	private String associationUserRoleName;

	private AccountMapperHelper<DtField, GroupProperty> mapperHelper; //GroupProperty from UserGroupAttribute

	private enum GroupProperty {
		id, displayName
	}

	/**
	 * Constructor.
	 * @param userIdentityEntity Entity name of userIdentityEntity
	 * @param groupIdentityEntity Entity name of groupIdentityEntity
	 * @param userAuthField FieldName use to find user by it's authToken
	 * @param userToAccountMappingStr User to account conversion mapping
	 * @param groupToGroupAccountMappingStr User to account conversion mapping
	 * @param storeManager Store Manager
	 */
	@Inject
	public StoreAccountStorePlugin(
			@Named("userIdentityEntity") final String userIdentityEntity,
			@Named("groupIdentityEntity") final String groupIdentityEntity,
			@Named("userAuthField") final String userAuthField,
			@Named("userToAccountMapping") final String userToAccountMappingStr,
			@Named("groupToGroupAccountMapping") final String groupToGroupAccountMappingStr,
			final StoreManager storeManager) {
		super(userIdentityEntity, userToAccountMappingStr);
		Assertion.checkArgNotEmpty(userIdentityEntity);
		Assertion.checkArgNotEmpty(userAuthField);
		Assertion.checkNotNull(storeManager);
		Assertion.checkArgNotEmpty(groupToGroupAccountMappingStr);
		this.groupIdentityEntity = groupIdentityEntity;
		this.userAuthField = userAuthField;
		this.storeManager = storeManager;
		this.groupToGroupAccountMappingStr = groupToGroupAccountMappingStr;
	}

	@Override
	protected void postStart() {
		userGroupDtDefinition = Home.getApp().getDefinitionSpace().resolve(groupIdentityEntity, DtDefinition.class);
		mapperHelper = new AccountMapperHelper(userGroupDtDefinition, GroupProperty.class, groupToGroupAccountMappingStr)
				.withMandatoryDestField(GroupProperty.id)
				.withMandatoryDestField(GroupProperty.displayName)
				.parseAttributeMapping();

		for (final AssociationDefinition association : Home.getApp().getDefinitionSpace().getAll(AssociationDefinition.class)) {
			if ((userGroupDtDefinition.equals(association.getAssociationNodeA().getDtDefinition())
					&& getUserDtDefinition().equals(association.getAssociationNodeB().getDtDefinition()))) {
				associationUserGroup = association;
				associationUserRoleName = association.getAssociationNodeB().getRole();
				associationGroupRoleName = association.getAssociationNodeA().getRole();
				break;
			} else if (userGroupDtDefinition.equals(association.getAssociationNodeB().getDtDefinition())
					&& getUserDtDefinition().equals(association.getAssociationNodeA().getDtDefinition())) {
				associationUserGroup = association;
				associationUserRoleName = association.getAssociationNodeA().getRole();
				associationGroupRoleName = association.getAssociationNodeB().getRole();
				break;
			}
		}
		Assertion.checkNotNull(associationUserGroup, "Association between User ({0}) and Group ({1}) not found", getUserDtDefinition().getClassSimpleName(), userGroupDtDefinition.getClassSimpleName());
		Assertion.checkState(associationUserGroup instanceof AssociationSimpleDefinition || associationUserGroup instanceof AssociationNNDefinition, "Association ({0}) between User and Group must be an AssociationSimpleDefinition or an AssociationNNDefinition", associationUserGroup.getName());
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

	/** {@inheritDoc} */
	@Override
	public Set<URI<AccountGroup>> getGroupURIs(final URI<Account> accountURI) {
		final DtListURI groupDtListURI;
		if (associationUserGroup instanceof AssociationSimpleDefinition) {
			groupDtListURI = new DtListURIForSimpleAssociation((AssociationSimpleDefinition) associationUserGroup, accountURI, associationGroupRoleName);
		} else { //autres cas éliminés par assertion dans le postStart
			Assertion.checkArgument(associationUserGroup instanceof AssociationNNDefinition, "Association ({0}) between User and Group must be an AssociationSimpleDefinition or an AssociationNNDefinition", associationUserGroup.getName());
			groupDtListURI = new DtListURIForNNAssociation((AssociationNNDefinition) associationUserGroup, accountURI, associationGroupRoleName);
		}
		//-----
		final DtList<? extends Entity> result = io.vertigo.app.Home.getApp().getComponentSpace().resolve(io.vertigo.dynamo.store.StoreManager.class).getDataStore().findAll(groupDtListURI);
		return result.stream()
				.map(groupEntity -> groupToAccount(groupEntity).getURI())
				.collect(Collectors.toSet());
	}

	/** {@inheritDoc} */
	@Override
	public long getGroupsCount() {
		return storeManager.getDataStore().count(userGroupDtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public AccountGroup getGroup(final URI<AccountGroup> accountGroupURI) {
		final URI<Entity> groupURI = new URI(userGroupDtDefinition, accountGroupURI.getId());
		final Entity groupEntity = storeManager.getDataStore().readOne(groupURI);
		return groupToAccount(groupEntity);
	}

	/** {@inheritDoc} */
	@Override
	public Set<URI<Account>> getAccountURIs(final URI<AccountGroup> groupURI) {
		final DtListURI userDtListURI;
		if (associationUserGroup instanceof AssociationSimpleDefinition) {
			userDtListURI = new DtListURIForSimpleAssociation((AssociationSimpleDefinition) associationUserGroup, groupURI, associationUserRoleName);
		} else { //autres cas éliminés par assertion dans le postStart
			Assertion.checkArgument(associationUserGroup instanceof AssociationNNDefinition, "Association ({0}) between User and Group must be an AssociationSimpleDefinition or an AssociationNNDefinition", associationUserGroup.getName());
			userDtListURI = new DtListURIForNNAssociation((AssociationNNDefinition) associationUserGroup, groupURI, associationUserRoleName);
		}
		//-----
		final DtList<? extends Entity> result = io.vertigo.app.Home.getApp().getComponentSpace().resolve(io.vertigo.dynamo.store.StoreManager.class).getDataStore().findAll(userDtListURI);
		return result.stream()
				.map(userEntity -> userToAccount(userEntity).getURI())
				.collect(Collectors.toSet());
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final URI<Account> accountURI) {
		return Optional.empty(); //TODO
	}

	/** {@inheritDoc} */
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

	protected AccountGroup groupToAccount(final Entity groupEntity) {
		final String groupId = parseAttribute(String.class, GroupProperty.id, groupEntity);
		final String displayName = parseAttribute(String.class, GroupProperty.displayName, groupEntity);
		return new AccountGroup(groupId, displayName);
	}

	private <O> O parseAttribute(final Class<O> valueClass, final GroupProperty accountProperty, final Entity userEntity) {
		final DtField attributeField = mapperHelper.getSourceAttribute(accountProperty);
		return valueClass.cast(attributeField.getDataAccessor().getValue(userEntity));
	}

}

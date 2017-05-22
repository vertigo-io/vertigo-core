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
package io.vertigo.account.data;

import java.util.Arrays;
import java.util.List;

import io.vertigo.account.identity.Account;
import io.vertigo.account.identity.AccountGroup;
import io.vertigo.account.identity.IdentityManager;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.util.ListBuilder;

public final class Identities {

	private Identities() {
		//rien
	}

	public static URI<Account> createAccountURI(final String id) {
		return DtObjectUtil.createURI(Account.class, id);
	}

	public static URI<AccountGroup> createGroupURI(final String id) {
		return DtObjectUtil.createURI(AccountGroup.class, id);
	}

	public static void initData(final IdentityManager identityManager) {
		final Account testAccount0 = Account.builder("0").withAuthToken("john.doe").withDisplayName("John doe").withEmail("john.doe@yopmail.com").build();
		final Account testAccount1 = Account.builder("1").withAuthToken("palmer.luckey").withDisplayName("Palmer Luckey").withEmail("palmer.luckey@yopmail.com").build();
		final Account testAccount2 = Account.builder("2").withAuthToken("bill.clinton").withDisplayName("Bill Clinton").withEmail("bill.clinton@yopmail.com").build();
		identityManager.getStore().saveAccounts(Arrays.asList(testAccount0, testAccount1, testAccount2));

		final URI<Account> accountURI0 = createAccountURI(testAccount0.getId());
		final URI<Account> accountURI1 = createAccountURI(testAccount1.getId());
		final URI<Account> accountURI2 = createAccountURI(testAccount2.getId());

		final AccountGroup testAccountGroup1 = new AccountGroup("100", "TIME's cover");
		final URI<AccountGroup> group1Uri = DtObjectUtil.createURI(AccountGroup.class, testAccountGroup1.getId());
		identityManager.getStore().saveGroup(testAccountGroup1);

		identityManager.getStore().attach(accountURI1, group1Uri);
		identityManager.getStore().attach(accountURI2, group1Uri);

		final AccountGroup groupAll = new AccountGroup("ALL", "Everyone");
		final URI<AccountGroup> groupAllUri = DtObjectUtil.createURI(AccountGroup.class, groupAll.getId());
		identityManager.getStore().saveGroup(groupAll);
		identityManager.getStore().attach(accountURI0, groupAllUri);
		identityManager.getStore().attach(accountURI1, groupAllUri);
		identityManager.getStore().attach(accountURI2, groupAllUri);

		//---create 10 noisy data
		final List<Account> accounts = createAccounts();
		identityManager.getStore().saveAccounts(accounts);
		for (final Account account : accounts) {
			final URI<Account> accountUri = createAccountURI(account.getId());
			identityManager.getStore().attach(accountUri, groupAllUri);
		}

	}

	private static int SEQ_ID = 10;

	private static List<Account> createAccounts() {
		return new ListBuilder<Account>()
				.add(createAccount("Jean Meunier", "jmeunier@yopmail.com"))
				.add(createAccount("Emeline Granger", "egranger@yopmail.com"))
				.add(createAccount("Silvia Robert", "sylv.robert@yopmail.com"))
				.add(createAccount("Manuel Long", "manu@yopmail.com"))
				.add(createAccount("David Martin", "david.martin@yopmail.com"))
				.add(createAccount("Véronique LeBourgeois", "vero89@yopmail.com"))
				.add(createAccount("Bernard Dufour", "bdufour@yopmail.com"))
				.add(createAccount("Nicolas Legendre", "nicolas.legendre@yopmail.com"))
				.add(createAccount("Marie Garnier", "marie.garnier@yopmail.com"))
				.add(createAccount("Hugo Bertrand", "hb@yopmail.com"))
				.build();
	}

	private static Account createAccount(final String displayName, final String email) {
		return Account.builder(Integer.toString(SEQ_ID++))
				.withAuthToken(email.substring(0, email.indexOf('@')))
				.withDisplayName(displayName)
				.withEmail(email)
				.build();
	}
}

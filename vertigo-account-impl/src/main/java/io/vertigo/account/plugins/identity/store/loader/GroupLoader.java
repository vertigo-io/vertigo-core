package io.vertigo.account.plugins.identity.store.loader;

import java.util.Set;

import io.vertigo.account.identity.Account;
import io.vertigo.account.identity.AccountGroup;
import io.vertigo.dynamo.domain.model.URI;

/**
 * @author npiedeloup
 */
public interface GroupLoader {

	/**
	 * @return the number of groups.
	 */
	long getGroupsCount();

	/**
	 * Gets the group defined by an URI.
	 * @param groupURI the group URI
	 * @return the group
	 */
	AccountGroup getGroup(URI<AccountGroup> groupURI);

	/**
	 * @param accountURI the account defined by its URI
	 * @return Set of groups of this account
	 */
	Set<URI<AccountGroup>> getGroupURIs(URI<Account> accountURI);

	/**
	 * Lists the accounts for a defined group.
	 * @param groupURI the group URI
	 * @return the list of acccounts.
	 */
	Set<URI<Account>> getAccountURIs(URI<AccountGroup> groupURI);

}

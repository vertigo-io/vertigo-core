package io.vertigo.account.plugins.identity.store.loader;

import java.util.Optional;

import io.vertigo.account.identity.Account;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.VFile;

/**
 * @author npiedeloup
 */
public interface AccountLoader {

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
	 * Gets the photo of an account defined by its URI.
	 *
	 * @param accountURI the account defined by its URI
	 * @return the photo as a file
	 */
	Optional<VFile> getPhoto(URI<Account> accountURI);

	/**
	 * Get an newly authentify user by his authToken.
	 * @param userAuthToken user authToken
	 * @return Logged account
	 */
	Optional<Account> getAccountByAuthToken(String userAuthToken);
}

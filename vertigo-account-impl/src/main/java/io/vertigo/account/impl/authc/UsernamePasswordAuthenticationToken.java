
package io.vertigo.account.impl.authc;

import io.vertigo.account.authc.AuthenticationToken;
import io.vertigo.lang.Assertion;

/**
 * @author npiedeloup
 */
public class UsernamePasswordAuthenticationToken implements AuthenticationToken {

	private final PasswordHelper passwordHelper;

	/**
	 * The username
	 */
	private final String username;

	/**
	 * The encoded password
	 */
	private final String password;

	/**
	 * @param username the principal submitted for authentication
	 * @param password the password string submitted for authentication
	 */
	public UsernamePasswordAuthenticationToken(final String username, final String password) {
		Assertion.checkArgNotEmpty(username);
		Assertion.checkArgNotEmpty(password);
		//----
		this.username = username;
		this.password = password;
		passwordHelper = new PasswordHelper();
	}

	/** {@inheritDoc} */
	@Override
	public String getPrincipal() {
		return username;
	}

	/**
	 * Returns the password submitted during an authentication attempt
	 *
	 * @return the password submitted during an authentication attempt.
	 */
	public String getPassword() {
		return password;
	}

	/** {@inheritDoc} */
	@Override
	public boolean match(final AuthenticationToken trustedAuthenticationToken) {
		if (trustedAuthenticationToken instanceof UsernamePasswordAuthenticationToken) {
			return ((UsernamePasswordAuthenticationToken) trustedAuthenticationToken).getPrincipal().equals(username)
					&& passwordHelper.checkPassword(((UsernamePasswordAuthenticationToken) trustedAuthenticationToken).getPassword(), password);
		}
		return false;
	}
}

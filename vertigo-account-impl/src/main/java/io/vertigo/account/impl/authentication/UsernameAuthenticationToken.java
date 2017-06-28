
package io.vertigo.account.impl.authentication;

import io.vertigo.account.authentication.AuthenticationToken;
import io.vertigo.lang.Assertion;

/**
 * @author npiedeloup
 */
public final class UsernameAuthenticationToken implements AuthenticationToken {

	/**
	 * The username
	 */
	private final String username;

	/**
	 * @param username the username submitted for authentication
	 */
	public UsernameAuthenticationToken(final String username) {
		Assertion.checkArgNotEmpty(username);
		//----
		this.username = username;
	}

	/** {@inheritDoc} */
	@Override
	public String getPrincipal() {
		return username;
	}

	/** {@inheritDoc} */
	@Override
	public boolean match(final AuthenticationToken trustedAuthenticationToken) {
		if (trustedAuthenticationToken instanceof UsernameAuthenticationToken) {
			return trustedAuthenticationToken.getPrincipal().equals(username);
		}
		return false;
	}

}

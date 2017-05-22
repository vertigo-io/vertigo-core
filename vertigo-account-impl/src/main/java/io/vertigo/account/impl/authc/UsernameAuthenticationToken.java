
package io.vertigo.account.impl.authc;

import io.vertigo.account.authc.AuthenticationToken;
import io.vertigo.lang.Assertion;

/**
 * @author npiedeloup
 */
public class UsernameAuthenticationToken implements AuthenticationToken {

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
			return ((UsernameAuthenticationToken) trustedAuthenticationToken).getPrincipal().equals(username);
		}
		return false;
	}

}

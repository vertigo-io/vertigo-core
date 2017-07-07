package io.vertigo.account.authentication;

/**
 * Authentification token.
 * @author npiedeloup
 */
public interface AuthenticationToken {

	/**
	 * @return the username submitted during an authentication attempt.
	 */
	String getPrincipal();

	/**
	 * Check if this User-submitted AuthenticationToken matches the realm trustedAuthenticationToken.
	 * @param trustedAuthenticationToken realm trustedAuthenticationToken
	 * @return if this token matches
	 */
	boolean match(AuthenticationToken trustedAuthenticationToken);
}

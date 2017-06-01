package io.vertigo.account.authentification;

/**
 * Authentification token.
 * @author npiedeloup
 */
public interface AuthenticationToken {

	/**
	 * Returns the username submitted during an authentication attempt.
	 * @return the username submitted during an authentication attempt.
	 */
	String getPrincipal();

	/**
	 * Check if this User-submitted AuthenticationToken match the realm trustedAuthenticationToken.
	 * @param trustedAuthenticationToken realm trustedAuthenticationToken
	 * @return if these token matches
	 */
	boolean match(AuthenticationToken trustedAuthenticationToken);
}

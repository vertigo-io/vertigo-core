package io.vertigo.account.plugins.authentification.text;

import io.vertigo.account.authentification.AuthenticationToken;
import io.vertigo.lang.Assertion;

final class AuthenticationAccountInfo {
	private final String accountKey;
	private final AuthenticationToken authenticationToken;

	AuthenticationAccountInfo(final String accountKey, final AuthenticationToken authenticationToken) {
		Assertion.checkNotNull(accountKey);
		Assertion.checkNotNull(authenticationToken);
		//-----
		this.accountKey = accountKey;
		this.authenticationToken = authenticationToken;
	}

	String getAccountKey() {
		return accountKey;
	}

	AuthenticationToken getAuthenticationToken() {
		return authenticationToken;
	}
}

package io.vertigo.account.plugins.identity.text;

import io.vertigo.account.identity.Account;
import io.vertigo.lang.Assertion;

final class IdentityAccountInfo {

	private final Account account;
	private final String photoUrl;

	IdentityAccountInfo(final Account account, final String photoUrl) {
		Assertion.checkNotNull(account);
		Assertion.checkNotNull(photoUrl);
		//-----
		this.account = account;
		this.photoUrl = photoUrl;
	}

	Account getAccount() {
		return account;
	}

	String getPhotoUrl() {
		return photoUrl;
	}
}

package io.vertigo.studio.plugins.mda.authorization.model;

import java.util.Optional;

import io.vertigo.account.authorization.metamodel.Authorization;
import io.vertigo.lang.Assertion;

public class AuthorizationModel {

	private final Authorization authorization;

	public AuthorizationModel(final Authorization authorization) {
		Assertion.checkNotNull(authorization);
		//---
		this.authorization = authorization;
	}

	public String getName() {
		return authorization.getName();
	}

	public Optional<String> getComment() {
		return authorization.getComment();
	}

	public String getOperationName() {
		return authorization.getOperation().get();
	}

}

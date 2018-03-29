package io.vertigo.studio.plugins.mda.authorization.model;

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.account.authorization.metamodel.SecuredEntity;
import io.vertigo.lang.Assertion;

public class SecuredEntityModel {

	private final SecuredEntity securedEntity;
	private final List<AuthorizationModel> authorizationModels;

	public SecuredEntityModel(final SecuredEntity securedEntity) {
		Assertion.checkNotNull(securedEntity);
		//---
		this.securedEntity = securedEntity;
		authorizationModels = securedEntity.getOperations().stream()
				.map(AuthorizationModel::new)
				.collect(Collectors.toList());
	}

	public String getClassSimpleName() {
		return securedEntity.getEntity().getClassSimpleName();
	}

	public String getClassCanonicalName() {
		return securedEntity.getEntity().getClassCanonicalName();
	}

	public List<AuthorizationModel> getOperations() {
		return authorizationModels;
	}

}

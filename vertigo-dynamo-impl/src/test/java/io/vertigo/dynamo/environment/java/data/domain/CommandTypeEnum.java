package io.vertigo.dynamo.environment.java.data.domain;

import java.io.Serializable;

import io.vertigo.dynamo.domain.model.MasterDataEnum;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

public enum CommandTypeEnum implements MasterDataEnum<io.vertigo.dynamo.environment.java.data.domain.CommandType> {

	ferme("1"), //
	optionel("2"), //
	titi("3");

	private final URI<io.vertigo.dynamo.environment.java.data.domain.CommandType> entityUri;

	private CommandTypeEnum(final Serializable id) {
		entityUri = DtObjectUtil.createURI(io.vertigo.dynamo.environment.java.data.domain.CommandType.class, id);
	}

	@Override
	public URI<io.vertigo.dynamo.environment.java.data.domain.CommandType> getEntityUri() {
		return entityUri;
	}

}

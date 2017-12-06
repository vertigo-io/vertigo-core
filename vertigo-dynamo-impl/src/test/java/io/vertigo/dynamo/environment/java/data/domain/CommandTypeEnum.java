package io.vertigo.dynamo.environment.java.data.domain;

import java.io.Serializable;

import io.vertigo.dynamo.domain.model.MasterDataEnum;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

public enum CommandTypeEnum implements MasterDataEnum<CommandType> {

	ferme("1"), //
	optionelle("2"), //
	provisoire("3");

	private final URI<CommandType> entityUri;

	private CommandTypeEnum(final Serializable id) {
		entityUri = DtObjectUtil.createURI(CommandType.class, id);
	}

	@Override
	public URI<CommandType> getEntityUri() {
		return entityUri;
	}

}

package io.vertigo.dynamo.store.data.domain.car;

import java.io.Serializable;

import io.vertigo.dynamo.domain.model.MasterDataEnum;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

public enum MotorTypeEnum implements MasterDataEnum<MotorType> {

	essence("ESSENCE"), //
	diesel("DIESEL");

	private final URI<MotorType> entityUri;

	private MotorTypeEnum(final Serializable id) {
		entityUri = DtObjectUtil.createURI(MotorType.class, id);
	}

	@Override
	public URI<MotorType> getEntityUri() {
		return entityUri;
	}

}

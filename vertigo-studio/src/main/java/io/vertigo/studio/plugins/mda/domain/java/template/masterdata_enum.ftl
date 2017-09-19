package ${entity.packageName};

import java.io.Serializable;

import io.vertigo.dynamo.domain.model.MasterDataEnum;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

public enum ${entity.classSimpleName}Enum implements MasterDataEnum<${entity.className}> {

<#list entity.enumValues as enumValue>
	${enumValue.name}("${enumValue.id}")<#sep>, //</#sep>
</#list>	;

	private final URI<${entity.className}> entityUri;

	private ${entity.classSimpleName}Enum(final Serializable id) {
		entityUri = DtObjectUtil.createURI(${entity.className}.class, id);
	}

	@Override
	public URI<${entity.className}> getEntityUri() {
		return entityUri;
	}

}

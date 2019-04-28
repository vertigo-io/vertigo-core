package io.vertigo.dynamo.store.datastore;

import java.util.function.Predicate;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.lang.Assertion;

@DefinitionPrefix("Md")
public class MasterDataDefinition implements Definition {

	private final String name;
	private final DtListURIForMasterData uri;
	private final Predicate predicate;

	public MasterDataDefinition(
			final String name,
			final DtListURIForMasterData uri,
			final Predicate predicate) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(predicate);
		//---
		this.name = name;
		this.uri = uri;
		this.predicate = predicate;
	}

	@Override
	public String getName() {
		return name;
	}

	public DtListURIForMasterData getUri() {
		return uri;
	}

	public Predicate getPredicate() {
		return predicate;
	}

}

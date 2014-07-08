package io.vertigo.labs.trait;

import io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.annotation.Field;

@DtDefinition(persistent = false)
public final class Tagging implements Trait {
	private static final long serialVersionUID = -1369516323733642838L;
	@Field(domain = "DO_TAGGING", notNull = true, label = "Tagging")
	private String tags;

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getTags() {
		return tags;
	}
}

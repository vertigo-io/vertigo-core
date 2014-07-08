package io.vertigo.labs.trait;

import io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.annotation.Field;

@DtDefinition(persistent = false)
public final class Commenting implements Trait {
	private static final long serialVersionUID = -1369516323733642838L;
	@Field(domain = "DO_COMMENTING", notNull = true, label = "Commenting")
	private String comments;

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getComments() {
		return comments;
	}
}

package io.vertigo.labs.trait;

import io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.annotation.Field;

@DtDefinition(persistent = false)
public final class Rating implements Trait {
	private static final long serialVersionUID = -1369516323733642838L;
	@Field(domain = "DO_RATING", notNull = true, label = "Rating")
	private Integer rate;

	public void setRate(Integer rate) {
		this.rate = rate;
	}

	public Integer getRate() {
		return rate;
	}
}

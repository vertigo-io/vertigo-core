package io.vertigo.rest.engine;

import io.vertigo.dynamo.domain.model.DtObject;

public class OData<O extends DtObject> {
	
	private final O filter;
	
	private final long top;
	private final long skip;
	private final String orderBy;
	private final String format;
	private final long inlineCount;
	
	OData(O filter, long top, long skip,String orderBy, String format, long inlineCount) {
		this.filter = filter;
		this.top = top;
		this.skip = skip;
		this.orderBy = orderBy;
		this.format = format;
		this.inlineCount = inlineCount;
	}
	
	
	public O getFilter() {
		return filter;
	}
	public long getTop() {
		return top;
	}
	public long getSkip() {
		return skip;
	}
	public String getOrderBy() {
		return orderBy;
	}
	public String getFormat() {
		return format;
	}
	public long getInlineCount() {
		return inlineCount;
	}
	
	
}

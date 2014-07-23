package io.vertigo.rest.rest.engine;

import io.vertigo.dynamo.domain.model.DtObject;

public class OData<O extends DtObject> {
	
	private final O filter;
	
	private final long top;
	private final long skip;
	private final String orderBy;
	private final String format;
	private final long inlineCount;
	private final long totalCount;
	
	public OData(O filter, long top, long skip,String orderBy, String format, long inlineCount, long totalCount) {
		this.filter = filter;
		this.top = top;
		this.skip = skip;
		this.orderBy = orderBy;
		this.format = format;
		this.inlineCount = inlineCount;
		this.totalCount = totalCount;
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
	public long getTotalCount() {
		return totalCount;
	}
	
	
}

package io.vertigo.rest;

public final class ListCriteria {

	private long from;
	private long size;
	private String sortFieldName;
	private boolean sortDesc;

	public long getFrom() {
		return from;
	}

	public void setFrom(final long from) {
		this.from = from;
	}

	public long getSize() {
		return size;
	}

	public void setSize(final long size) {
		this.size = size;
	}

	public String getSortFieldName() {
		return sortFieldName;
	}

	public void setSortFieldName(final String sortFieldName) {
		this.sortFieldName = sortFieldName;
	}

	public boolean isSortDesc() {
		return sortDesc;
	}

	public void setSortDesc(final boolean sortDesc) {
		this.sortDesc = sortDesc;
	}

}

package io.vertigo.rest.engine;

public final class UiListState {
	private final int top;
	private final int skip;
	private final String sortFieldName;
	private final boolean sortDesc;

	public UiListState(final int top, final int skip, final String sortFieldName, final boolean sortDesc) {
		this.top = top;
		this.skip = skip;
		this.sortFieldName = sortFieldName;
		this.sortDesc = sortDesc;
	}

	public int getTop() {
		return top;
	}

	public int getSkip() {
		return skip;
	}

	public String getSortFieldName() {
		return sortFieldName;
	}

	public boolean isSortDesc() {
		return sortDesc;
	}

}

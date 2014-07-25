package io.vertigo.rest.rest.metamodel;

import java.io.Serializable;

/**
 * List state : page and sort infos.
 * @author npiedeloup (22 juil. 2014 17:31:15)
 */
public final class UiListState implements Serializable {
	private static final long serialVersionUID = 1343925518488701786L;
	private final int top;
	private final int skip;
	private final String sortFieldName;
	private final boolean sortDesc;

	//reference the previous serverToken of the list. Use to sort and paginate a snapshot of list. 
	private final String listServerToken;

	/**
	 * @param top max returning elements
	 * @param skip elements to skip
	 * @param sortFieldName sort fieldName
	 * @param sortDesc desc or asc order
	 * @param listServerToken reference the previous serverToken of the list
	 */
	public UiListState(final int top, final int skip, final String sortFieldName, final boolean sortDesc, final String listServerToken) {
		this.top = top;
		this.skip = skip;
		this.sortFieldName = sortFieldName;
		this.sortDesc = sortDesc;
		this.listServerToken = listServerToken;
	}

	/**
	 * @return nb returning elements
	 */
	public int getTop() {
		return top;
	}

	/**
	 * @return nb elements to skip
	 */
	public int getSkip() {
		return skip;
	}

	/**
	 * @return sort fieldName
	 */
	public String getSortFieldName() {
		return sortFieldName;
	}

	/**
	 * @return  desc or asc order
	 */
	public boolean isSortDesc() {
		return sortDesc;
	}

	/**
	 * @return serverToken
	 */
	public String getListServerToken() {
		return listServerToken;
	}

}

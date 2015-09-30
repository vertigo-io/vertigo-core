package io.vertigo.dynamox.search.dsl.definition;

public final class DslUserCriteria {
	private final String preMissingPart;
	private final String overridedFieldName;
	private final String overridedPreModifier;
	private final String criteriaWord;
	private final String overridedPostModifier;
	private final String postMissingPart;

	public DslUserCriteria(final String preMissingPart, final String overridedFieldName, final String overridedPreModifier, final String criteriaWord, final String overridedPostModifier, final String postMissingPart) {
		this.preMissingPart = preMissingPart;
		this.overridedFieldName = overridedFieldName;
		this.overridedPreModifier = overridedPreModifier;
		this.criteriaWord = criteriaWord;
		this.overridedPostModifier = overridedPostModifier;
		this.postMissingPart = postMissingPart;
	}

	/**
	 * @return preMissingPart
	 */
	public final String getPreMissingPart() {
		return preMissingPart;
	}

	/**
	 * @return overridedFieldName
	 */
	public final String getOverridedFieldName() {
		return overridedFieldName;
	}

	/**
	 * @return overridedPreModifier
	 */
	public final String getOverridedPreModifier() {
		return overridedPreModifier;
	}

	/**
	 * @return criteriaWord
	 */
	public final String getCriteriaWord() {
		return criteriaWord;
	}

	/**
	 * @return overridedPostModifier
	 */
	public final String getOverridedPostModifier() {
		return overridedPostModifier;
	}

	/**
	 * @return postMissingPart
	 */
	public final String getPostMissingPart() {
		return postMissingPart;
	}
}

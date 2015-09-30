package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;

/**
 * One user criteria.
 * A user query is many DslUserCriteria.
 * @author npiedeloup
 */
public final class DslUserCriteria {
	private final String preMissingPart;
	private final String overridedFieldName;
	private final String overridedPreModifier;
	private final String criteriaWord;
	private final String overridedPostModifier;
	private final String postMissingPart;

	/**
	 * @param preMissingPart Optional pre missing part
	 * @param overridedFieldName Optional overrided fieldName
	 * @param overridedPreModifier Optional overrided word pre modifier
	 * @param criteriaWord Criteria word
	 * @param overridedPostModifier Optional overrided word post modifier
	 * @param postMissingPart Optional post missing part
	 */
	public DslUserCriteria(final String preMissingPart, final String overridedFieldName, final String overridedPreModifier, final String criteriaWord, final String overridedPostModifier, final String postMissingPart) {
		Assertion.checkNotNull(preMissingPart);
		Assertion.checkNotNull(overridedFieldName);
		Assertion.checkNotNull(overridedPreModifier);
		Assertion.checkArgNotEmpty(criteriaWord);
		Assertion.checkNotNull(overridedPostModifier);
		Assertion.checkNotNull(postMissingPart);
		//-----
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

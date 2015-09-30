package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;

import java.util.List;

/**
 * Multi expressions definition.
 * (preBody)\(?(expression|multiExpression)+\)?(postBody)
 * @author npiedeloup
 */
public final class DslMultiExpressionDefinition {

	private final String preBody; //Spaces like
	private final boolean block;
	private final List<DslExpressionDefinition> expressions;
	private final List<DslMultiExpressionDefinition> multiExpressions;
	private final String postBody; //Spaces like

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(preBody).append(block ? "(" : "");
		for (final DslExpressionDefinition expression : expressions) {
			sb.append(expression);
		}
		for (final DslMultiExpressionDefinition multiExpression : multiExpressions) {
			sb.append(multiExpression);
		}
		sb.append(block ? ")" : "").append(postBody);
		return sb.toString();
	}

	/**
	 * @param preBody String before body
	 * @param block Is mode block
	 * @param expressions List of simple expression
	 * @param multiExpressions List of multi-expression
	 * @param postBody String after body
	 */
	public DslMultiExpressionDefinition(final String preBody,
			final boolean block, final List<DslExpressionDefinition> expressions,
			final List<DslMultiExpressionDefinition> multiExpressions,
			final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(expressions);
		Assertion.checkNotNull(multiExpressions);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.block = block;
		this.expressions = expressions;
		this.multiExpressions = multiExpressions;
		this.postBody = postBody;
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
	}

	/**
	 * @return block
	 */
	public final boolean isBlock() {
		return block;
	}

	/**
	 * @return expressions
	 */
	public final List<DslExpressionDefinition> getExpressions() {
		return expressions;
	}

	/**
	 * @return multiExpressions
	 */
	public final List<DslMultiExpressionDefinition> getMultiExpressions() {
		return multiExpressions;
	}

	/**
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}

}

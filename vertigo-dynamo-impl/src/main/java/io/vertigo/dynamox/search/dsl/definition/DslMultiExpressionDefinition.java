package io.vertigo.dynamox.search.dsl.definition;

import java.util.List;

public final class DslMultiExpressionDefinition {

	//(preMultiExpression)
	//(expression|multiExpression)+
	//(postMultiExpression)

	private final String preMultiExpression; //Spaces like
	private final boolean block;
	private final List<DslExpressionDefinition> expressions;
	private final List<DslMultiExpressionDefinition> multiExpressions;
	private final String postMultiExpression; //Spaces like

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(preMultiExpression).append(block ? "(" : "");
		for (final DslExpressionDefinition expression : expressions) {
			sb.append(expression);
		}
		for (final DslMultiExpressionDefinition multiExpression : multiExpressions) {
			sb.append(multiExpression);
		}
		sb.append(block ? ")" : "").append(postMultiExpression);
		return sb.toString();
	}

	public DslMultiExpressionDefinition(final String preMultiExpression,
			final boolean block, final List<DslExpressionDefinition> expressions,
			final List<DslMultiExpressionDefinition> multiExpressions,
			final String postMultiExpression) {
		this.preMultiExpression = preMultiExpression;
		this.block = block;
		this.expressions = expressions;
		this.multiExpressions = multiExpressions;
		this.postMultiExpression = postMultiExpression;
	}

	/**
	 * @return preMultiExpression
	 */
	public final String getPreMultiExpression() {
		return preMultiExpression;
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
	 * @return postMultiExpression
	 */
	public final String getPostMultiExpression() {
		return postMultiExpression;
	}

}

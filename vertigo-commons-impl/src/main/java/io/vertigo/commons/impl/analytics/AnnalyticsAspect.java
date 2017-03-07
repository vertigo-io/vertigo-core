package io.vertigo.commons.impl.analytics;

import javax.inject.Inject;

import io.vertigo.commons.analytics.Analytics;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.aop.AspectMethodInvocation;
import io.vertigo.lang.Assertion;

/**
 * * Intercepteur de performance des composant.
 *
 * @author jmforhan
 */
public class AnnalyticsAspect implements Aspect {
	private final AnalyticsManager analyticsManager;

	/**
	 * Constructor.
	 * @param analyticsManager the component responsible of managing analytics
	 */
	@Inject
	public AnnalyticsAspect(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//---
		this.analyticsManager = analyticsManager;
	}

	/** {@inheritDoc} */
	@Override
	public Class<Analytics> getAnnotationType() {
		return Analytics.class;
	}

	/** {@inheritDoc} */
	@Override
	public Object invoke(final Object[] args, final AspectMethodInvocation invocation) {
		final Analytics analytics = invocation.getMethod().getAnnotation(Analytics.class);
		return analyticsManager.traceWithReturn(
				analytics.channel(),
				analytics.category(),
				tracer -> invocation.proceed(args));
	}
}

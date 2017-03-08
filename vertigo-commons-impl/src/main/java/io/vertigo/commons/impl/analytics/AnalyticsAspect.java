package io.vertigo.commons.impl.analytics;

import javax.inject.Inject;

import io.vertigo.commons.analytics.Analytics;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.aop.AspectMethodInvocation;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * * Intercepteur de performance des composant.
 *
 * @author jmforhan
 */
public class AnalyticsAspect implements Aspect {
	private final AnalyticsManager analyticsManager;

	/**
	 * Constructor.
	 * @param analyticsManager the component responsible of managing analytics
	 */
	@Inject
	public AnalyticsAspect(final AnalyticsManager analyticsManager) {
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
		//Aspect must be declared on methods or on the class.
		final Analytics analytics = invocation.getMethod().getAnnotation(Analytics.class) == null ? invocation.getMethod().getDeclaringClass().getAnnotation(Analytics.class)
				: invocation.getMethod().getAnnotation(Analytics.class);

		final String name = StringUtil.isEmpty(analytics.name()) ? invocation.getMethod().getDeclaringClass().getSimpleName() + "::" + invocation.getMethod().getName() : analytics.name();
		return analyticsManager.traceWithReturn(
				analytics.category(),
				name,
				tracer -> invocation.proceed(args));
	}
}

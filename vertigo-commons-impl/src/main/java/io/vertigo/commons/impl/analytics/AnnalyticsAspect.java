package io.vertigo.commons.impl.analytics;

import javax.inject.Inject;

import io.vertigo.commons.analytics.Analytics;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTrackerWritable;
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
		try (AnalyticsTrackerWritable trackerWritable = analyticsManager.createTracker(analytics.processType(), analytics.category())) {
			try {
				final Object o = invocation.proceed(args);
				trackerWritable.markAsSucceeded();
				return o;
			} catch (final Exception e) {
				trackerWritable.markAsFailed(e);
				throw e;
			}
		}

	}
}

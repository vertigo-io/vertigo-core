/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2021, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.impl.analytics.trace;

import javax.inject.Inject;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.trace.Trace;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.aspect.Aspect;
import io.vertigo.core.node.component.aspect.AspectMethodInvocation;
import io.vertigo.core.util.StringUtil;

/**
 * * Intercepteur de performance des composant.
 *
 * @author jmforhan
 */
public final class TraceAspect implements Aspect {
	private final AnalyticsManager analyticsManager;

	/**
	 * Constructor.
	 * @param analyticsManager the component responsible of managing analytics
	 */
	@Inject
	public TraceAspect(final AnalyticsManager analyticsManager) {
		Assertion.check().isNotNull(analyticsManager);
		//---
		this.analyticsManager = analyticsManager;
	}

	/** {@inheritDoc} */
	@Override
	public Class<Trace> getAnnotationType() {
		return Trace.class;
	}

	/** {@inheritDoc} */
	@Override
	public Object invoke(final Object[] args, final AspectMethodInvocation invocation) {
		//Aspect must be declared on methods or on the class.
		final Trace analytics = invocation.getMethod().getAnnotation(Trace.class) == null ? invocation.getMethod().getDeclaringClass().getAnnotation(Trace.class)
				: invocation.getMethod().getAnnotation(Trace.class);

		final String name = StringUtil.isBlank(analytics.name()) ? invocation.getMethod().getDeclaringClass().getSimpleName() + "::" + invocation.getMethod().getName() : analytics.name();
		return analyticsManager.traceWithReturn(
				analytics.category(),
				name,
				tracer -> invocation.proceed(args));
	}
}

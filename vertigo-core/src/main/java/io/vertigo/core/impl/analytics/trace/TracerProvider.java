/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2022, Vertigo.io, team@vertigo.io
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

import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

import io.vertigo.core.analytics.trace.TraceSpan;
import io.vertigo.core.analytics.trace.Tracer;
import io.vertigo.core.lang.Assertion;

public class TracerProvider {

	/**
	 * Processus binde sur le thread courant. Le processus , recoit les notifications des sondes placees dans le code de
	 * l'application pendant le traitement d'une requete (thread).
	 */
	private static final ThreadLocal<Stack<TracerImpl>> THREAD_LOCAL_PROCESS = new ThreadLocal<>();

	public static void trace(final String category, final String name, final Consumer<Tracer> consumer, final Consumer<TraceSpan> onCloseConsumer) {
		try (TracerImpl tracer = createTracer(category, name, onCloseConsumer)) {
			try {
				consumer.accept(tracer);
				tracer.markAsSucceeded();
			} catch (final Exception e) {
				tracer.markAsFailed(e);
				throw e;
			}
		}
	}

	public static <O> O traceWithReturn(final String category, final String name, final Function<Tracer, O> function, final Consumer<TraceSpan> onCloseConsumer) {
		try (TracerImpl tracer = createTracer(category, name, onCloseConsumer)) {
			try {
				final O result = function.apply(tracer);
				tracer.markAsSucceeded();
				return result;
			} catch (final Exception e) {
				tracer.markAsFailed(e);
				throw e;
			}
		}
	}

	public static Optional<Tracer> getCurrentTracer() {
		// When collect feature is enabled
		return doGetCurrentTracer().map(Function.identity()); // convert impl to api
	}

	private static Optional<TracerImpl> doGetCurrentTracer() {
		if (THREAD_LOCAL_PROCESS.get() == null || THREAD_LOCAL_PROCESS.get().isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(THREAD_LOCAL_PROCESS.get().peek());
	}

	private static void push(final TracerImpl analyticstracer) {
		Assertion.check().isNotNull(analyticstracer);
		//---
		if (THREAD_LOCAL_PROCESS.get() == null) {
			THREAD_LOCAL_PROCESS.set(new Stack<>());
		}
		Assertion.check().isTrue(THREAD_LOCAL_PROCESS.get().size() < 100, "More than 100 process deep. All processes must be closed.");
		THREAD_LOCAL_PROCESS.get().push(analyticstracer);
	}

	private static Optional<TracerImpl> removeCurrentAndGetParentTracer() {
		THREAD_LOCAL_PROCESS.get().pop();
		final Optional<TracerImpl> parentOpt = doGetCurrentTracer();
		if (parentOpt.isEmpty()) {
			THREAD_LOCAL_PROCESS.remove();
		}
		return parentOpt;
	}

	private static TracerImpl createTracer(final String category, final String name, final Consumer<TraceSpan> onCloseConsumer) {
		final TracerImpl analyticsTracer = new TracerImpl(category, name, onCloseConsumer, TracerProvider::removeCurrentAndGetParentTracer);
		push(analyticsTracer);
		return analyticsTracer;
	}

}

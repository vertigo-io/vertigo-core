package io.vertigo.dynamo.impl.work;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkProcessor;

import java.util.Arrays;
/**
 * 
 * @author pchretien
 */
final class WorkProcessorImpl<WR,W> implements WorkProcessor<WR, W>{
	private final WorkManager workManager;
	private final WorkEngineProvider[] workEngineProviders;

	WorkProcessorImpl(final WorkManager workManager, final WorkEngineProvider workEngineProvider){
		this(workManager, new WorkEngineProvider[] {workEngineProvider});
		//-----------------------------------------------------------------
		Assertion.checkNotNull(workEngineProvider);
	}

	private WorkProcessorImpl(final WorkManager workManager, final WorkEngineProvider[] workEngineProviders){
		Assertion.checkNotNull(workManager);
		Assertion.checkNotNull(workEngineProviders);
		//-----------------------------------------------------------------
		this.workEngineProviders = workEngineProviders;
		this.workManager = workManager;
	}

	/** {@inheritDoc} */
	public <WR1> WorkProcessor<WR1, W> add(final WorkEngineProvider<WR1, WR> workEngineProvider) {
		Assertion.checkNotNull(workEngineProvider);
		//-----------------------------------------------------------------
		final WorkEngineProvider[] list= Arrays.copyOf(workEngineProviders, workEngineProviders.length+1);
		list[workEngineProviders.length]=workEngineProvider;
		return new WorkProcessorImpl<>(workManager, list);
	}

	/** {@inheritDoc} */
	public WR exec(final W input) {
		Object result = input;
		for (final WorkEngineProvider workEngineProvider:workEngineProviders){
			result = workManager.process(result, workEngineProvider);
		}
		return (WR) result;
	}
}
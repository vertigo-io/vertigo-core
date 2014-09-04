package io.vertigo.dynamo.work;

/**
 * 
 * @author pchretien
 */
public interface WorkProcessor<WR,W> {
	/**
	 * Add a task to the current processor to build a new processor
	 */
	<WR1> WorkProcessor<WR1, W> add(final WorkEngineProvider<WR1, WR> workEngineProvider);

	/**
	 * Execute processor composed of tasks.
	 * @param input
	 * @return output
	 */
	WR exec(W input);
}

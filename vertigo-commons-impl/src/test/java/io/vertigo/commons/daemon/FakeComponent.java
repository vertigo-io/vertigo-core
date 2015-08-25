package io.vertigo.commons.daemon;

import io.vertigo.lang.Component;

import javax.inject.Inject;

public class FakeComponent implements Component {
	int executions = 0;

	@Inject
	public FakeComponent(final DaemonManager daemonManager) {
		daemonManager.registerDaemon("simple", SimpleDaemon.class, 2);
	}

	public int getExecutionCount() {
		return executions;
	}

	void execute() {
		executions++;
		if (executions == 1) {
			throw new RuntimeException();
		}
	}

	public static final class SimpleDaemon implements Daemon {
		@Inject
		private FakeComponent fakeComponent;

		/** {@inheritDoc} */
		@Override
		public void run() throws Exception {
			fakeComponent.execute();
		}
	}
}

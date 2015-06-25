package io.vertigo.commons.daemon;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author TINGARGIOLA
 */
public final class DaemonManagerTest extends AbstractTestCaseJU4 {

	@Inject
	private DaemonManager daemonManager;

	@Override
	public void doSetUp() {
		final DaemonDefinition daemonDefinition = new DaemonDefinition("DMN_SIMPLE", SimpleDaemon.class, 3);
		Home.getDefinitionSpace().put(daemonDefinition);
	}

	@Test
	public void testSimple() throws Exception {
		Assert.assertEquals(0, SimpleDaemon.executions);
		// -----
		daemonManager.startAllDaemons();
		Assert.assertEquals(0, SimpleDaemon.executions);
		Thread.sleep(3000);
		Assert.assertTrue(SimpleDaemon.executions > 0);
	}

	public static class SimpleDaemon implements Daemon {

		static int executions = 0;

		/** {@inheritDoc} */
		@Override
		public void run() throws Exception {
			executions++;
		}
	}
}

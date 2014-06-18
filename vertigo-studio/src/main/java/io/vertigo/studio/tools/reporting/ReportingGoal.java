package io.vertigo.studio.tools.reporting;

import io.vertigo.kernel.Home;
import io.vertigo.studio.reporting.ReportingManager;
import io.vertigo.studio.tools.Goal;

import java.util.Properties;

/**
 * @author pchretien
 */
public final class ReportingGoal implements Goal {

	public void process(final Properties properties) {
		final ReportingManager reportingManager = Home.getComponentSpace().resolve(ReportingManager.class);

		reportingManager.analyze();
	}
}

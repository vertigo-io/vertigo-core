package io.vertigo.commons.node;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.app.Home;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;

public abstract class AbstractNodeManagerTest extends AbstractTestCaseJU4 {

	protected AppConfigBuilder buildRootAppConfig() {
		return AppConfig.builder()
				.beginBoot()
				.endBoot()
				.withNodeConfig(NodeConfig.builder()
						.withAppName("nodeTestApp")
						.withNodeId("nodeTest1")
						.build())
				.addModule(ModuleConfig.builder("db")
						.build());
	}

	@Test
	void testRegisterNode() {
		final NodeManager nodeManager = Home.getApp().getComponentSpace().resolve(NodeManager.class);

		final List<Node> nodesWithDbSkill = nodeManager.locateSkills("db");
		final List<Node> nodesWithOtherSkill = nodeManager.locateSkills("other");

		// ---
		Assertions.assertEquals(1, nodesWithDbSkill.size());
		Assertions.assertEquals(0, nodesWithOtherSkill.size());

	}

	@Test
	void testUpdate() throws InterruptedException {
		final NodeManager nodeManager = Home.getApp().getComponentSpace().resolve(NodeManager.class);
		// ---
		final Instant firstTouch = nodeManager.find("nodeTest1").get().getLastTouch();
		Thread.sleep(7 * 1000L);
		final Instant secondTouch = nodeManager.find("nodeTest1").get().getLastTouch();
		// ---
		Assertions.assertTrue(secondTouch.isAfter(firstTouch));

	}

}

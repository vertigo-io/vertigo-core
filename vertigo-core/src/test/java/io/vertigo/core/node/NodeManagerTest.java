package io.vertigo.core.node;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.app.Home;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.ModuleConfig;

@RunWith(JUnitPlatform.class)
public class NodeManagerTest extends AbstractTestCaseJU4 {

	@Override
	protected AppConfig buildAppConfig() {
		return AppConfig.builder()
				.beginBoot()
				.endBoot()
				.addModule(ModuleConfig.builder("db")
						.build())
				.build();
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

}

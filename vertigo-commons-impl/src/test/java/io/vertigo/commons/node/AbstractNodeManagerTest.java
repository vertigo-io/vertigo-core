package io.vertigo.commons.node;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.app.Home;

public abstract class AbstractNodeManagerTest extends AbstractTestCaseJU4 {

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

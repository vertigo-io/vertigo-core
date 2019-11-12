package io.vertigo.studio.tasktest;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.studio.data.tasktest.DaoTestClass;

public class PingTest extends DaoTestClass {

	@Inject
	private DaoPAO daoPAO;

	@Test
	public void check_ping_Ok() {
		check().semantics(() -> daoPAO.ping());
	}
}

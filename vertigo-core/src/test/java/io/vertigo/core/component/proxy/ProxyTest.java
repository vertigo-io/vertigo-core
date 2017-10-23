package io.vertigo.core.component.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.component.proxy.data.Aggregate;

@RunWith(JUnitPlatform.class)
public final class ProxyTest extends AbstractTestCaseJU4 {
	@Inject
	private Aggregate aggregatea;

	@Test
	public final void testMin() {
		assertEquals(10, aggregatea.min(12, 10, 55));
		assertEquals(10, aggregatea.min(10, 55));
		assertEquals(10, aggregatea.min(10));
	}

	@Test
	public final void testMax() {
		assertEquals(55, aggregatea.max(12, 10, 55));
		assertEquals(55, aggregatea.max(10, 55));
		assertEquals(55, aggregatea.max(55));
	}

	@Test
	public final void testCount() {
		assertEquals(3, aggregatea.count(12, 10, 55));
		assertEquals(2, aggregatea.count(10, 55));
		assertEquals(1, aggregatea.count(55));
	}
}

package io.vertigo.util;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public final class MapBuilderTest {
	@Test
	public void testMap() {
		final Map<String, Integer> map = new MapBuilder<String, Integer>()
				.put("one", 1)
				.put("two", 2)
				.put("three", 3)
				.unmodifiable()
				.build();

		Assert.assertEquals(3, map.size());
		Assert.assertEquals(2, map.get("two").intValue());
	}

	@Test
	public void testModifiableMap() {
		final Map<String, Integer> map = new MapBuilder<String, Integer>()
				.put("one", 1)
				.put("two", 2)
				.put("three", 3)
				.build();

		map.put("nine", 9);
		Assert.assertEquals(4, map.size());
	}

	@Test(expected = Exception.class)
	public void testUnmodifiableMap() {
		final Map<String, Integer> map = new MapBuilder<String, Integer>()
				.put("one", 1)
				.put("two", 2)
				.put("three", 3)
				.unmodifiable()
				.build();

		map.put("nine", 9);
	}
}

package io.vertigo.kernel.home.definitionspace;

import io.vertigo.AbstractTestCase2JU4;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

import org.junit.Assert;
import org.junit.Test;

public class DefinitionSpaceTest extends AbstractTestCase2JU4 {

	@Override
	protected void configMe(final ComponentSpaceConfigBuilder componentSpaceConfiguilder) {
		// @formatter:off
		final ComponentSpaceConfig componentSpaceConfig = new ComponentSpaceConfigBuilder()
			.withParam("log4j.configurationFileName", "/log4j.xml")
			.withSilence(false)
		.build();
		// @formatter:on
	}

	@Test
	public void testEmpty() {
		Assert.assertEquals("definitionSpace must be emmpty", 0L, Home.getDefinitionSpace().getAllTypes().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRegisterIsMandatory() {
		Assert.assertEquals("definitionSpace must be emmpty", 0L, Home.getDefinitionSpace().getAllTypes().size());
		Home.getDefinitionSpace().put(new SampleDefinition(), SampleDefinition.class);
	}

	@Test
	public void testRegister() {
		Assert.assertEquals("definitionSpace must be emmpty", 0L, Home.getDefinitionSpace().getAllTypes().size());
		Home.getDefinitionSpace().register(SampleDefinition.class);
		Home.getDefinitionSpace().put(new SampleDefinition(), SampleDefinition.class);

		Assert.assertEquals("definitionSpace must contain one element ", 1L, Home.getDefinitionSpace().getAllTypes().size());
		Assert.assertEquals("definitionSpace[SampleDefinition.class] must contain one element ", 1L, Home.getDefinitionSpace().getAll(SampleDefinition.class).size());
	}

	@Prefix("SAMPLE")
	public static class SampleDefinition implements Definition {

		public String getName() {
			return "SAMPLE_DEFINITION";
		}
	}
}

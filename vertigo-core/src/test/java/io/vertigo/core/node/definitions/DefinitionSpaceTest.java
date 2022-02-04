/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2022, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.node.definitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionId;
import io.vertigo.core.node.definition.DefinitionPrefix;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.param.Param;

public final class DefinitionSpaceTest extends AbstractTestCaseJU5 {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("test")
						.addDefinitionProvider(DefinitionProviderConfig.builder(TestDefinitionprovider.class)
								.addParam(Param.of("testParam", "testParamValue"))
								.addDefinitionResource("type1", "resource1")
								.build())
						.build())
				.build();
	}

	@Test
	public void testRegister() throws IOException, ClassNotFoundException {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		assertEquals(3L, definitionSpace.getAllTypes().size(), "definitionSpace must contain three elements ");
		assertEquals(1L, definitionSpace.getAll(SampleDefinition.class).size(), "definitionSpace[SampleDefinition.class] must contain one element ");

		final SampleDefinition sampleDefinition = definitionSpace.resolve("SampleTheDefinition", SampleDefinition.class);
		assertNotNull(sampleDefinition);
		assertEquals("TheDefinition", sampleDefinition.id().shortName(), "localName must be TheDefinition");
		assertEquals(sampleDefinition.getName(), SampleDefinition.PREFIX + sampleDefinition.id().shortName(),
				"globalName must be SampleTheDefinition");

		final DefinitionId<SampleDefinition> sampleDefinitionId = sampleDefinition.id();

		byte[] serialized;
		try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(); final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(sampleDefinitionId);
			oos.flush();
			serialized = bos.toByteArray();
		}

		//---
		final DefinitionId definitionId2;
		try (final ByteArrayInputStream bis = new ByteArrayInputStream(serialized); final ObjectInputStream ios = new ObjectInputStream(bis)) {
			definitionId2 = DefinitionId.class.cast(ios.readObject());
		}

		assertNotSame(sampleDefinitionId, definitionId2, "DefinitionIds must be not strictly equals");
		assertSame(sampleDefinition, definitionId2.get(), "Definitions must be strictly equals");
	}

	@DefinitionPrefix(SampleDefinition.PREFIX)
	public static class SampleDefinition extends AbstractDefinition<SampleDefinition> {
		public static final String PREFIX = "Sample";

		SampleDefinition() {
			super("SampleTheDefinition");
		}
	}
}

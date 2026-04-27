/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.data.BioManager;
import io.vertigo.core.node.component.data.BioManagerImpl;
import io.vertigo.core.node.component.data.MathManager;
import io.vertigo.core.node.component.data.MathManagerImpl;
import io.vertigo.core.node.component.data.SimpleMathPlugin;
import io.vertigo.core.param.Param;

/**
 * Tests de configurations invalides.
 * Verifie que le framework detecte correctement les erreurs de configuration
 * et leve les exceptions appropriees.
 *
 * @author pchretien
 */
public final class InvalidConfigTest {

	// --- Parametres manquants ---

	@Test
	public void testMissingRequiredParam() {
		// MathManagerImpl necessite le param "start" qui n'est pas fourni
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(MathManager.class, MathManagerImpl.class)
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class, () -> {
			try (AutoCloseableNode _ = new AutoCloseableNode(nodeConfig)) {
				// ne devrait pas arriver ici
			}
		});
	}

	@Test
	public void testMissingRequiredPluginParam() {
		// SimpleMathPlugin necessite le param "factor" qui n'est pas fourni
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(SimpleMathPlugin.class)
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class, () -> {
			try (AutoCloseableNode _ = new AutoCloseableNode(nodeConfig)) {
				// ne devrait pas arriver ici
			}
		});
	}

	// --- Dependances manquantes ---

	@Test
	public void testMissingDependency() {
		// BioManagerImpl a besoin de MathManager qui n'est pas declare
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(BioManager.class, BioManagerImpl.class)
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class, () -> {
			try (AutoCloseableNode _ = new AutoCloseableNode(nodeConfig)) {
				// ne devrait pas arriver ici
			}
		});
	}

	@Test
	public void testMissingPlugin() {
		// MathManagerImpl a besoin d'un MathPlugin qui n'est pas declare
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class, () -> {
			try (AutoCloseableNode _ = new AutoCloseableNode(nodeConfig)) {
				// ne devrait pas arriver ici
			}
		});
	}

	// --- Parametres de mauvais type ---

	@Test
	public void testWrongParamType() {
		// Le param "start" attend un int mais on fournit une chaine non numerique
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "not_a_number"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class, () -> {
			try (AutoCloseableNode _ = new AutoCloseableNode(nodeConfig)) {
				// ne devrait pas arriver ici
			}
		});
	}

	// --- Configuration du module vide ---

	@Test
	public void testModuleWithBlankName() {
		Assertions.assertThrows(RuntimeException.class, () -> {
			ModuleConfig.builder("").build();
		});
	}

	// --- NodeConfig sans locales reste valide ---

	@Test
	public void testNodeWithoutLocales() {
		// Un node sans locales est valide (pas de LocaleManager)
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			final MathManager mathManager = node.getComponentSpace().resolve(MathManager.class);
			Assertions.assertEquals(123, mathManager.add(1, 2));
		}
	}

	// --- Composant duplique ---

	@Test
	public void testDuplicateComponent() {
		// Deux composants avec la meme interface API
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.addModule(ModuleConfig.builder("bio2")
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "200"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "30"))
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class, () -> {
			try (AutoCloseableNode _ = new AutoCloseableNode(nodeConfig)) {
				// ne devrait pas arriver ici
			}
		});
	}

	// --- Plugin inutilise ---

	@Test
	public void testUnusedPlugin() {
		// Un plugin declare mais qui n'est injecte dans aucun composant
		// doit provoquer une erreur (deja teste dans ComponentSpaceTest.testHome2)
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(BioManager.class, BioManagerImpl.class)
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.addPlugin(io.vertigo.core.node.component.data.SimpleDummyPlugin.class)
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class, () -> {
			try (AutoCloseableNode _ = new AutoCloseableNode(nodeConfig)) {
				// ne devrait pas arriver ici
			}
		});
	}

	// --- Param non utilise ---

	@Test
	public void testUnusedParam() {
		// Un parametre qui n'est pas consomme par le composant
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"),
								Param.of("unknownParam", "value"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class, () -> {
			try (AutoCloseableNode _ = new AutoCloseableNode(nodeConfig)) {
				// ne devrait pas arriver ici
			}
		});
	}

	// --- Resolve d'un composant inexistant ---

	@Test
	public void testResolveUnknownComponent() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("bio")
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			Assertions.assertThrows(RuntimeException.class, () -> {
				node.getComponentSpace().resolve(BioManager.class);
			});
		}
	}
}

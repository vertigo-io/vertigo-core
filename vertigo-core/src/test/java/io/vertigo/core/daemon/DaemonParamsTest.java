/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.daemon;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.NodeConfig;

public final class DaemonParamsTest {

	@Test
	public void testMaxBelowDefaultMinNamesTheParams() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withDaemonMaxDurationInSeconds(60)
						.build())
				.build();
		// -----
		final Exception exception = assertThrows(Exception.class, () -> {
			try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
				//nothing
			}
		});
		final String message = rootCause(exception).getMessage();
		assertTrue(message.contains("daemonMinDurationInSeconds (120)") && message.contains("daemonMaxDurationInSeconds (60)"), message);
	}

	private static Throwable rootCause(final Throwable throwable) {
		Throwable cause = throwable;
		while (cause.getCause() != null && cause.getCause() != cause) {
			cause = cause.getCause();
		}
		return cause;
	}

}

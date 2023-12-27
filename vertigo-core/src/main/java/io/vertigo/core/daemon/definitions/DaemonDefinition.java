/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.daemon.definitions;

import java.util.function.Supplier;

import io.vertigo.core.daemon.Daemon;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;

/**
 * Represents the definition of a daemon.
 *
 * This class provides information and configuration for a specific daemon.
 *
 * @author: mlaroche, pchretien, npiedeloup
 *
 * @see io.vertigo.core.daemon.Daemon
 * @see io.vertigo.core.node.definition.AbstractDefinition
 */
@DefinitionPrefix(DaemonDefinition.PREFIX)
public final class DaemonDefinition extends AbstractDefinition<DaemonDefinition> {
	public static final String PREFIX = "Dmn";

	private final int periodInSeconds;
	private final Supplier<Daemon> daemonSupplier;

	/**
     * Constructs a DaemonDefinition with the specified name, daemon supplier, and execution period.
     *
     * @param name the name of the daemon
     * @param daemonSupplier the supplier providing instances of the daemon
     * @param periodInSeconds the execution period of the daemon in seconds
     */
	public DaemonDefinition(final String name, final Supplier<Daemon> daemonSupplier, final int periodInSeconds) {
		super(name);
		//---
		Assertion.check()
				.isNotNull(daemonSupplier)
				.isTrue(periodInSeconds > 0, "period {0} must be > 0", periodInSeconds);
		//---
		this.daemonSupplier = daemonSupplier;
		this.periodInSeconds = periodInSeconds;
	}

	/**
     * Gets the execution period of the daemon in seconds.
     *
     * @return the execution period in seconds
     */
	public int getPeriodInSeconds() {
		return periodInSeconds;
	}
	
	/**
     * Gets the supplier providing instances of the daemon.
     *
     * @return the daemon supplier
     */
	public Supplier<Daemon> getDaemonSupplier() {
		return daemonSupplier;
	}
}

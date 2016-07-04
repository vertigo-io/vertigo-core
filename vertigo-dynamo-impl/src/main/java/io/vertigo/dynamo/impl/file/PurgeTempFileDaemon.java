/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.dynamo.impl.file;

import java.io.File;

import io.vertigo.commons.daemon.Daemon;

/**
 * Purge store directory Daemon.
 */
public final class PurgeTempFileDaemon implements Daemon {
	private final int purgeDelayMinutes;
	private final String documentRoot;

	/**
	 * @param purgeDelayMinutes Purge files older than this delay in minutes
	 * @param documentRoot Purge scan root
	 */
	public PurgeTempFileDaemon(final int purgeDelayMinutes, final String documentRoot) {
		this.purgeDelayMinutes = purgeDelayMinutes;
		this.documentRoot = documentRoot;
	}

	/** {@inheritDoc} */
	@Override
	public void run() throws Exception {
		final File documentRootFile = new File(documentRoot);
		final long maxTime = System.currentTimeMillis() - purgeDelayMinutes * 60L * 1000L;
		purgeOlderFile(documentRootFile, maxTime);
	}

	private static void purgeOlderFile(final File documentRootFile, final long maxTime) {
		for (final File subFiles : documentRootFile.listFiles()) {
			if (subFiles.isDirectory() && subFiles.canRead()) { //canRead pour les pbs de droits
				purgeOlderFile(subFiles, maxTime);
			} else if (subFiles.lastModified() < maxTime) {
				if (!subFiles.delete()) {
					subFiles.deleteOnExit();
				}
			}
		}
	}
}

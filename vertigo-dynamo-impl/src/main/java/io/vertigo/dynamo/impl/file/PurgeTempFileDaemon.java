package io.vertigo.dynamo.impl.file;

import io.vertigo.commons.daemon.Daemon;

import java.io.File;

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

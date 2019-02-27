package io.vertigo.studio.plugins.mda.util;

import java.io.File;

import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.MdaResultBuilder;

public class MdaUtil {

	private MdaUtil() {
		// util
	}

	private static boolean deleteDirectory(final File directory, final MdaResultBuilder mdaResultBuilder) {
		deleteFiles(directory, mdaResultBuilder);
		return (directory.delete());
	}

	public static void deleteFiles(final File directory, final MdaResultBuilder mdaResultBuilder) {
		if (directory.exists()) {
			Assertion.checkArgument(directory.isDirectory(), "targetGenDir must be a directory");
			for (final File file : directory.listFiles()) {
				if (file.isDirectory()) {
					deleteDirectory(file, mdaResultBuilder);
				} else {
					file.delete(); // we don't care about real deletion of the file
					mdaResultBuilder.incFileDeleted();
				}
			}
		}
	}

}

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
package io.vertigo.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;

public final class FileUtil {

	private static final String USER_CHECK_ERROR_MSG = "User try to use illegal fileName";

	private static final String USER_HOME = "user.home";
	private static final String USER_DIR = "user.dir";
	private static final String USER_HOME_PATH = System.getProperty(USER_HOME).replace('\\', '/');
	private static final String USER_DIR_PATH = System.getProperty(USER_DIR).replace('\\', '/');

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	private static final String JAVA_IO_TMPDIR_PATH = System.getProperty(JAVA_IO_TMPDIR).replace('\\', '/');

	private static final String KEY_USER_HOME = "\\$\\{" + USER_HOME + "\\}";
	private static final String KEY_USER_DIR = "\\$\\{" + USER_DIR + "\\}";
	private static final String KEY_JAVA_IO_TMPDIR = "\\$\\{" + JAVA_IO_TMPDIR + "\\}";

	private static final Pattern PATTERN_USER_HOME = Pattern.compile(KEY_USER_HOME);
	private static final Pattern PATTERN_USER_DIR = Pattern.compile(KEY_USER_DIR);
	private static final Pattern PATTERN_JAVA_IO_TMPDIR = Pattern.compile(KEY_JAVA_IO_TMPDIR);

	private static final Pattern FILENAME_EXCLUSIONS_PATTERN;

	static {
		final String[] exclusions = {
				// think to escape each character
				"\\\\", "\\/", "\\:", "\\*", "\\?", "\\\"", "\\<", "\\>", "\\|", "\\{", "\\}"
		};
		final String regex = Arrays.stream(exclusions)
				.collect(Collectors.joining("|"));
		FILENAME_EXCLUSIONS_PATTERN = Pattern.compile(regex);
	}

	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private FileUtil() {
		//rien
	}

	public static <R extends Object> R doOnFile(final URL url, final Function<Path, R> fileProcessor) {
		Assertion.check().isNotNull(url);
		Assertion.check().isNotNull(fileProcessor);
		//---
		try {
			final var uri = url.toURI();
			if ("jar".equalsIgnoreCase(uri.getScheme())) {
				try (final FileSystem fs = FileSystems.newFileSystem(uri, Map.of())) { // need to "open" the jar for Paths.get to work
					return fileProcessor.apply(Paths.get(uri));
				}
			}
			return fileProcessor.apply(Paths.get(uri));
		} catch (final IOException | URISyntaxException e) {
			throw WrappedException.wrap(e, "Error when reading file : '{0}'", url);
		}
	}

	public static String read(final URL url) {
		Assertion.check().isNotNull(url);
		//---
		return doOnFile(url, path -> {
			try {
				return doRead(path);
			} catch (final IOException e) {
				throw WrappedException.wrap(e, "Error when reading file : '{0}'", url);
			}
		});

	}

	private static String doRead(final Path path) throws IOException {
		final List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		return String.join("\r\n", lines);
	}

	public static long getFileSize(final URL url) {
		Assertion.check().isNotNull(url);
		//---
		return doOnFile(url, path -> {
			try {
				return Files.size(path);
			} catch (final IOException e) {
				throw WrappedException.wrap(e, "Error when reading file : '{0}'", url);
			}
		});
	}

	/**
	 * Copie le contenu d'un flux d'entrée vers un fichier de sortie.
	 *
	 * @param in flux d'entrée
	 * @param file fichier de sortie
	 * @throws IOException Erreur d'entrée/sortie
	 */
	public static void copy(final InputStream in, final File file) throws IOException {
		Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Donne l'extension du fichier.
	 * <p>
	 * This method returns the textual part of the filename after the last dot.
	 * There must be no directory separator after the dot.
	 *
	 * <pre>
	 * foo.txt      --> "txt"
	 * a/b/c.jpg    --> "jpg"
	 * a/b.txt/c    --> ""
	 * a/b/c        --> ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param fileName Nom du fichier
	 * @return the extension of the file or an empty string if none exists.
	 *         (author Apache Commons IO 1.1)
	 */
	public static String getFileExtension(final String fileName) {
		final String extension;
		// The extension separator character.
		final char extensionSeparator = '.';
		// The Unix separator character.
		final char unixSeparator = '/';
		// The Windows separator character.
		final char windowsSeparator = '\\';
		final int extensionPos = fileName.lastIndexOf(extensionSeparator);
		final int lastUnixPos = fileName.lastIndexOf(unixSeparator);
		final int lastWindowsPos = fileName.lastIndexOf(windowsSeparator);
		final int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
		final int index = lastSeparator > extensionPos ? -1 : extensionPos;
		if (index == -1) {
			extension = "";
			// null dans la version cvs précédente
		} else {
			extension = fileName.substring(index + 1).toLowerCase(Locale.ENGLISH);
		}
		return extension;
	}

	/**
	 * Replace "user.home" "user.dir" and "java.io.tmpdir" by system value.
	 *
	 * @param path PAth to translate
	 * @return translated path
	 */
	public static String translatePath(final String path) {
		String translatedPath = PATTERN_USER_HOME.matcher(path).replaceAll(USER_HOME_PATH);
		translatedPath = PATTERN_USER_DIR.matcher(translatedPath).replaceAll(USER_DIR_PATH);
		translatedPath = PATTERN_JAVA_IO_TMPDIR.matcher(translatedPath).replaceAll(JAVA_IO_TMPDIR_PATH);
		return translatedPath;
	}

	/**
	 * Check a filePath send by a user.
	 *
	 * @param userPath Path to check
	 */
	public static void checkUserPath(final String userPath) {
		Assertion.check().isFalse(userPath.contains("..")
				&& userPath.indexOf((char) 0) == -1, //char 0
				USER_CHECK_ERROR_MSG);
	}

	/**
	 * Check a filename send by a user.
	 *
	 * @param userFileName FileName to check
	 */
	public static void checkUserFileName(final String userFileName) {
		Assertion.check().isTrue(userFileName.indexOf('\\') == -1 //Windows path_separator
				&& userFileName.indexOf('/') == -1 //Linux path_separator
				&& userFileName.indexOf((char) 0) == -1, //char 0
				USER_CHECK_ERROR_MSG);
	}

	public static String sanitizeFileName(final String fileName) {
		return FILENAME_EXCLUSIONS_PATTERN.matcher(fileName).replaceAll("_");
	}

}

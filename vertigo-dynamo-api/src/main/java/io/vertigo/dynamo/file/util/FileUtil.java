package io.vertigo.dynamo.file.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilitaire de gestion des fichiers et flux associés.
 * 
 * @author npiedeloup
 */
public final class FileUtil {
	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private FileUtil() {
		//rien
	}

	/**
	 * Copie le contenu d'un flux d'entrée vers un flux de sortie.
	 * @param in flux d'entrée
	 * @param out flux de sortie
	 * @throws IOException Erreur d'entrée/sortie
	 */
	public static void copy(final InputStream in, final OutputStream out) throws IOException {
		final int bufferSize = 10 * 1024;
		final byte[] bytes = new byte[bufferSize];
		int read = in.read(bytes);
		//		long offset = 0;
		while (read != -1) {
			//			final long start = System.currentTimeMillis();
			out.write(bytes, 0, read);
			read = in.read(bytes);
			//			offset += read;
			//			if (System.currentTimeMillis() - start > 1000) {
			//				System.out.println("Wait "+(System.currentTimeMillis()-start)+"ms at offset "+offset+" for "+read+"o");
			//			}
		}
	}

	/**
	 * Copie le contenu d'un flux d'entrée vers un fichier de sortie.
	 * @param in flux d'entrée
	 * @param file fichier de sortie
	 * @throws IOException Erreur d'entrée/sortie
	 */
	public static void copy(final InputStream in, final File file) throws IOException {
		try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			FileUtil.copy(in, out);
		}
	}

	/**
	 * Copie le contenu d'un fichier d'entrée vers un flux de sortie.
	 * @param fileIn fichier d'entrée
	 * @param fileOut fichier de sortie
	 * @throws KSystemException Exception système
		public static void copy(final File fileIn, final File fileOut) throws KSystemException {
			try {
				doCopy(fileIn, fileOut);
			} catch (final IOException e) {
				throw new KSystemException("Impossible de créer le fichier " + fileOut.getName(), e);
			}
		}
		private static void doCopy(final File fileIn, final File fileOut) throws IOException {
			final InputStream in = new FileInputStream(fileIn);
			try {
				final OutputStream out = new FileOutputStream(fileOut);
				try {
					FileUtil.copy(in, out);
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}
		}
	/**
	 * Copie le contenu d'un fichier d'entrée vers un flux de sortie.
	 * @param file fichier d'entrée
	 * @param out flux de sortie
	 * @throws KSystemException Exception système
		  	public static void copy(final File file, final OutputStream out) throws KSystemException {
	 
			InputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(file));
				FileUtil.copy(in, out);
			} catch (final IOException e) {
				throw new KSystemException("Impossible de lire le fichier " + file.getName(), e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (final IOException e) {
						throw new KSystemException("Impossible de cloturer le flux de lecture", e);
					}
				}
			}
		}
	*/

	/**
	 * Donne l'extension du fichier.
	 * <p>
	 * This method returns the textual part of the filename after the last dot.
	 * There must be no directory separator after the dot.
	 * <pre>
	 * foo.txt      --> "txt"
	 * a/b/c.jpg    --> "jpg"
	 * a/b.txt/c    --> ""
	 * a/b/c        --> ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * @param fileName Nom du fichier
	 *
	 * @return the extension of the file or an empty string if none exists.
	 * (author Apache Commons IO 1.1)
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
			extension = fileName.substring(index + 1).toLowerCase();
		}
		return extension;
	}
}

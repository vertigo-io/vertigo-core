/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.file;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.KFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Date;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'implémentation standard.
 *
 * @author dchallas
 */
public final class FileManagerTest extends AbstractTestCaseJU4 {

	@Inject
	private FileManager fileManager;

	/** {@inheritDoc} */
	@Override
	protected void doTearDown() {
		//rien
	}

	@Test
	public void testCreateTempFile() {
		final File file = TestUtil.getFile("data/testFile.txt", getClass());
		final KFile kFile = fileManager.createFile(file);
		checkFile(kFile, "testFile.txt", null, "text/plain", 71092L);
	}

	@Test
	public void testObtainReadOnlyFile() {
		final File file = TestUtil.getFile("data/testFile.txt", getClass());
		final KFile kFile = fileManager.createFile(file);
		checkFile(fileManager.obtainReadOnlyFile(kFile), file);
	}

	@Test
	public void testCreateTempFileWithFixedNameAndMime() {
		final String fileName = "monTestFile.txt";
		final String typeMime = "monTypeMime";
		final File file = TestUtil.getFile("data/testFile.txt", getClass());
		final KFile kFile = fileManager.createFile(fileName, typeMime, file);
		checkFile(kFile, fileName, null, typeMime, 71092L);
	}

	@Test
	public void testCreateTempFileWithNoFileNoMime() {
		final String fileName = "monTestFile.txt";
		final Date lastModified = new Date();
		final long length = 123;
		final InputStreamBuilder inputStreamBuilder = new InputStreamBuilder() {
			public InputStream createInputStream() {
				return new StringBufferInputStream("Contenu test");
			}
		};
		final KFile kFile = fileManager.createFile(fileName, lastModified, length, inputStreamBuilder);
		checkFile(kFile, fileName, lastModified, "text/plain", length);
	}

	@Test
	public void testCreateTempFileWithNoFile() {
		final String fileName = "monTestFile.txt";
		final String typeMime = "monTypeMime";
		final Date lastModified = new Date();
		final long length = 123;
		final InputStreamBuilder inputStreamBuilder = new InputStreamBuilder() {
			public InputStream createInputStream() {
				return new StringBufferInputStream("Contenu test");
			}
		};
		final KFile kFile = fileManager.createFile(fileName, typeMime, lastModified, length, inputStreamBuilder);
		checkFile(kFile, fileName, lastModified, typeMime, length);
	}

	//	public void testFileDefinition() {
	//		nop(FileMetaDefinition.getMetaDefinition("FI_FILE_INFO_STD"));
	//	}

	private void checkFile(final KFile kFile, final String fileName, final Date lastModified, final String mimeType, final Long length) {
		Assert.assertEquals(fileName, kFile.getFileName());
		if (lastModified != null) { //le lastModified peut être inconnu du test
			Assert.assertEquals(lastModified, kFile.getLastModified());
		}
		Assert.assertEquals(mimeType, kFile.getMimeType());
		Assert.assertEquals(length, kFile.getLength());

		try {
			nop(kFile.createInputStream());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void checkFile(final File outFile, final File inFile) {
		Assert.assertEquals(inFile.getAbsolutePath(), outFile.getAbsolutePath());
	}
}

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
package io.vertigo.dynamo.store.multistore;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.store.AbstractStoreManagerTest;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.dynamock.fileinfo.FileInfoTemp;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public final class MultiStoreManagerTest extends AbstractStoreManagerTest {

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		initOtherStore();
	}

	protected void initOtherStore() {
		//A chaque test on recrée la table famille dans l'autre base
		createDataBase(getCreateOtherStoreRequests(), "TK_INIT_OTHER", Optional.<String> of("otherStore"));
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		shutDown("TK_SHUT_DOWN_OTHER", Optional.<String> of("otherStore"));
	}

	@Override
	protected List<String> getCreateMainStoreRequests() {
		//On retire famille du main store
		final List<String> requests = getCreateCarRequests();
		requests.addAll(getCreateFileInfoRequests());
		return requests;
	}

	private List<String> getCreateOtherStoreRequests() {
		final List<String> requests = getCreateFamilleRequests();
		return requests;
	}

	@Test
	public void testOtherStoreFile() throws Exception {
		final VFile vFile = TestUtil.createVFile(fileManager, "data/lautreamont.txt", AbstractStoreManagerTest.class);
		//1.Création du fichier depuis un fichier texte du FS
		final FileInfo fileInfo = new FileInfoTemp(vFile);

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//2. Sauvegarde en Temp
			storeManager.getFileStore().create(fileInfo);
			transaction.commit(); //can't read file if not commited (TODO ?)
		}

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {

			//3.relecture du fichier
			final FileInfo readFileInfo = storeManager.getFileStore().read(fileInfo.getURI());

			//4. comparaison du fichier créé et du fichier lu.

			final String source;
			try (final OutputStream sourceOS = new java.io.ByteArrayOutputStream()) {
				FileUtil.copy(vFile.createInputStream(), sourceOS);
				source = sourceOS.toString();
			}

			final String read;
			try (final OutputStream readOS = new java.io.ByteArrayOutputStream()) {
				FileUtil.copy(readFileInfo.getVFile().createInputStream(), readOS);
				read = readOS.toString();
			}
			//on vérifie que le contenu des fichiers est identique.
			//assertEquals("toto", "toto");
			//assertEquals("toto", "ti");
			Assert.assertEquals(source, read);
			Assert.assertTrue("Test contenu du fichier", read.startsWith("Chant I"));
			Assert.assertTrue("Test contenu du fichier : " + secureSubString(read, 16711, "ses notes langoureuses,"), read.indexOf("ses notes langoureuses,") > 0);
			Assert.assertTrue("Test contenu du fichier : " + secureSubString(read, 11004, "mal : \"Adolescent,"), read.indexOf("mal : \"Adolescent,") > 0);

			//On désactive pour l'instant
			//Ne marche pas sur la PIC pour cause de charset sur le àé
			//Assert.assertTrue("Test contenu du fichier : " + secureSubString(read, 15579, "adieu !à ;"), read.indexOf("adieu !à ;") > 0);
		}
	}
}

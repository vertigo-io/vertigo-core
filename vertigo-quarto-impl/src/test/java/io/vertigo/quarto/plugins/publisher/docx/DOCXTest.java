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
package io.vertigo.quarto.plugins.publisher.docx;


/**
 * Classe pour les tests.
 * Publie les visibilités packages.
 * 
 * @author adufranne
 */
public final class DOCXTest {

	private DOCXTest() {
		// Non instanciable.
	}

	/**
	 * Teste le retour à la ligne pour les docx.
	 * 
	 * @param content le docx format texte.
	 * @return le xml
	 */
	public static String handleCarriageReturn(final String content) {
		final DOCXCleanerProcessor p = new DOCXCleanerProcessor();
		return p.execute(content, null);
	}

	/**
	 * Adaptation de la syntaxe utilisateur vers la syntaxe KSP.
	 * les <# #> sont rajoutés si ils sont manquants.
	 * 
	 * @param content le xml.
	 * @return le xml.
	 */
	public static String convertWrongFormattedTags(final String content) {
		final DOCXReverseInputProcessor p = new DOCXReverseInputProcessor();
		return p.execute(content, null);

	}

	/**
	 * Publication de la méthode de nettoyage des tags pour les tests.
	 * 
	 * @param content le xml.
	 * @return le xml.
	 */
	public static String cleanNotBESTags(final String content) {
		final DOCXReverseInputProcessor p = new DOCXReverseInputProcessor();
		return p.execute(content, null);
	}

	/**
	 * Publication de la méthode de factorisation des tags multiples pour les tests.
	 * 
	 * @param content le xml.
	 * @return le xml.
	 */
	public static String factorMultipleTags(final String content) {
		final DOCXReverseInputProcessor p = new DOCXReverseInputProcessor();
		return p.execute(content, null);
	}
}

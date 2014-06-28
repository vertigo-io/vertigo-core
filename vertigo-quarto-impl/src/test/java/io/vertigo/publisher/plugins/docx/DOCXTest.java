package io.vertigo.publisher.plugins.docx;

/**
 * Classe pour les tests.
 * Publie les visibilit�s packages.
 * 
 * @author adufranne
 * @version $Id: DOCXTest.java,v 1.1 2013/07/11 13:25:42 npiedeloup Exp $
 */
public final class DOCXTest {

	private DOCXTest() {
		// Non instanciable.
	}

	/**
	 * Teste le retour � la ligne pour les docx.
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
	 * les <# #> sont rajout�s si ils sont manquants.
	 * 
	 * @param content le xml.
	 * @return le xml.
	 */
	public static String convertWrongFormattedTags(final String content) {
		final DOCXReverseInputProcessor p = new DOCXReverseInputProcessor();
		return p.execute(content, null);

	}

	/**
	 * Publication de la m�thode de nettoyage des tags pour les tests.
	 * 
	 * @param content le xml.
	 * @return le xml.
	 */
	public static String cleanNotBESTags(final String content) {
		final DOCXReverseInputProcessor p = new DOCXReverseInputProcessor();
		return p.execute(content, null);
	}

	/**
	 * Publication de la m�thode de factorisation des tags multiples pour les tests.
	 * 
	 * @param content le xml.
	 * @return le xml.
	 */
	public static String factorMultipleTags(final String content) {
		final DOCXReverseInputProcessor p = new DOCXReverseInputProcessor();
		return p.execute(content, null);
	}
}

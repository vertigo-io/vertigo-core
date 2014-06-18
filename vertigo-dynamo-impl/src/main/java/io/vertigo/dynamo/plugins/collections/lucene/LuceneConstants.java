package io.vertigo.dynamo.plugins.collections.lucene;

/**
 * Centralisation du paramétrage de l'analyseur lucene.
 * 
 * @author  pchretien
 */
public final class LuceneConstants {

	private LuceneConstants() {
		//rien
	}

	/**
	 * Tableau des mots vides en Français et en Anglais.
	 */
	public static final String[] OUR_STOP_WORDS = { //
	//
			"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",//
			// Anglais
			"an", "and", "are", "as", "at", "be", "by", "for", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "with", "will", "over", "he", "she",
			// Français
			"et", "ou", "au", "aux", "le", "la", "les", "du", "des", "leur", "leurs", "dans", "en", "ce", "ca", "ces", "cet", "cette", "qui", "que", "quoi", "dont", "quel", "quels", "quelle", "quelles", "pour", "comme", "il", "elle", "lui", "par", "avec", "cela", "celui-ci", "celui-là", "celui", "ceux", "celles", "non", "oui", "sur", "car", "de", "donc", "dont", "elles", "est", "ils", "je", "lors", "me", "mes", "mon", "ni", "nos", "notre", "nous", "on", "sa", "se", "ses", "si", "son", "ta", "tes", "ton", "tous", "tout", "toutes", "tu", "un", "une", "unes", "uns", "voici", "voilà", "vos", "votre", "vous", "vôtre"// 
	};

	/**
	 * Tableau des elisions.
	 */
	public static final String[] ELISION_ARTICLES = { "l", "m", "t", "qu", "n", "s", "j" };
}

package io.vertigo.dynamo.plugins.collections.lucene;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ElisionFilter;
import org.apache.lucene.util.Version;

/**
 * Classe d'analyse des chaïnes de caractères.
 * Gestion des mots vides en français et en anglais.
 * @author  pchretien
 * @version $Id: DefaultAnalyzer.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
final class DefaultAnalyzer extends Analyzer implements Serializable {
	private static final long serialVersionUID = -653059693798148193L;
	private CharArraySet stopSet;

	/**
	 * Constructeur.
	 * @param useStopWord utilise les stopWord ou non
	 */
	DefaultAnalyzer(final boolean useStopWord) {
		this(useStopWord ? LuceneConstants.OUR_STOP_WORDS : new String[0]);
	}

	/** Builds an analyzer with the given stop words. */
	private DefaultAnalyzer(final String[] stopWords) {
		stopSet = StopFilter.makeStopSet(Version.LUCENE_40, stopWords);
	}

	/**
	   * Creates a TokenStream which tokenizes all the text in the provided Reader.
	   *
	   * @return A TokenStream build from a StandardTokenizer filtered with
	   *         StandardFilter, StopFilter, FrenchStemFilter and LowerCaseFilter
	   */
	@Override
	protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
		/* initialisation du token */
		final Tokenizer source = new StandardTokenizer(Version.LUCENE_40, reader);
		//---------------------------------------------------------------------
		/* on retire les élisions*/
		final CharArraySet elisionSet = new CharArraySet(Version.LUCENE_40, Arrays.asList(LuceneConstants.ELISION_ARTICLES), true);
		TokenStream filter = new ElisionFilter(source, elisionSet);
		/* on retire article adjectif */
		filter = new StopFilter(Version.LUCENE_40, filter, stopSet);
		/* on retire les accents */
		filter = new ASCIIFoldingFilter(filter);
		/* on met en minuscule */
		filter = new LowerCaseFilter(Version.LUCENE_40, filter);
		return new TokenStreamComponents(source, filter);
	}

	private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
		final boolean useStopWord = !stopSet.isEmpty();
		out.writeBoolean(useStopWord);
	}

	private void readObject(final java.io.ObjectInputStream in) throws IOException {
		final boolean useStopWord = in.readBoolean();
		stopSet = StopFilter.makeStopSet(Version.LUCENE_40, useStopWord ? LuceneConstants.OUR_STOP_WORDS : new String[0]);
	}

}

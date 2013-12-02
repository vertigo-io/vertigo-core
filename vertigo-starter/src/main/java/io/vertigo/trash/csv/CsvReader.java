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
package vertigo.trash.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import spark.commons.io.text.ReaderTextInput;
import spark.commons.io.text.TextInputParserContext;
import spark.commons.parser.ParseException;
import spark.commons.parser.ParserContext;

/**
 * Classe de lecture d'un fichier CSV.
 */
public class CsvReader implements Iterator<List<String>> {
    private static final char CR = '\r';
    private static final char LF = '\n';
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    private final char separator;
    private final char quote;

    private final ParserContext ctx;

    private final CsvPosition position;
    private CsvPosition recordPosition;
    private CsvPosition nextRecordPosition;
    private boolean hasNextRecord;
    private List<String> nextRecord;

    /**
     * Construit une nouvelle instance de CsvReader.
     * 
     * @param in Source reader.
     * @throws IOException In case of read error.
     */
    public CsvReader(Reader in) throws IOException {
        this(new TextInputParserContext(new ReaderTextInput(in)), DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    /**
     * Construit une nouvelle instance de CsvReader.
     * 
     * @param in Source reader.
     * @param separator Field separator.
     * @param quote Field quote.
     * @throws IOException In case of read error.
     */
    public CsvReader(Reader in, char separator, char quote) throws IOException {
        this(new TextInputParserContext(new ReaderTextInput(in)), separator, quote);
    }

    /**
     * Construit une nouvelle instance de CsvReader.
     * 
     * @param ctx Parser context.
     * @param separator Field separator.
     * @param quote Field quote.
     * @throws IOException In case of read error.
     */
    public CsvReader(ParserContext ctx, char separator, char quote) throws IOException {
        this.ctx = ctx;
        position = new CsvPosition(1);
        hasNextRecord = true;
        nextRecord = null;
        this.separator = separator;
        this.quote = quote;
    }

    /**
     * Donne la position courante dans le flux.
     * 
     * @return la position courante dans le flux.
     */
    public CsvPosition getPosition() {
        return position.copy();
    }

    /**
     * Donne la position courante dans le flux.
     * 
     * @return la position courante dans le flux.
     */
    public CsvPosition getRecordPosition() {
        return recordPosition;
    }

    /**
     * Indique s'il y a un autre enregistrement.
     * 
     * @return <code>true</code> s'il y a un autre enregsitrement.
     * @throws CsvException En cas d'erreur d'entr�-sortie.
     */
    public boolean hasNext() throws CsvException {
        try {
            // Si la valeur a d�j� �t� calcul�e, on passe.
            if (nextRecord == null && hasNextRecord) {
                if (ctx.isEmpty()) {
                    hasNextRecord = false;
                } else {
                    hasNextRecord = true;
                    parseRecord();
                }
            }

            return hasNextRecord;
        } catch (final Exception e) {
            throw new CsvException("Erreur de lecture", getPosition(), e);
        }
    }

    /**
     * Guve the next record.
     * 
     * @return The next record as a list of Strings array.
     * @throws CsvException When the file is not valid.
     * @throws NoSuchElementException If there is no more lines.
     */
    public List<String> next() throws CsvException, NoSuchElementException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // On avance
        List<String> record = nextRecord;
        recordPosition = nextRecordPosition;
        nextRecord = null;
        nextRecordPosition = null;

        return record;
    }

    /**
     * Ferme le lecteur de Fichiers CSV.
     * 
     * @throws IOException En cas d'erreur d'entr�-sortie.
     */
    public void close() throws IOException {
        ctx.close();
    }

    /**
     * M�thode non support�e.
     * 
     * @throws UnsupportedOperationException syst�matiquement.
     */
    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();

    }

    private void nextChar() {
        if (ctx.isNotEmpty()) {
            if (ctx.get() == LF) {
                position.addLine();
            } else {
                position.addChar();
            }
        }
        ctx.next();
    }

    private void parseRecord() {
        position.addRecord();
        nextRecordPosition = position.copy();
        nextRecord = new ArrayList<>();

        do {
            parseField();
        } while (parseSeparators());
        parseRecordSeparator();
    }

    private void parseField() {
        if (ctx.isNotEmpty()) {
            if (ctx.get() == quote) {
                parseFieldWithQuotes();
            } else {
                parseFieldWithoutQuotes();
            }
        }
    }

    private void parseFieldWithQuotes() {
        if (ctx.isEmpty() || ctx.get() != quote) {
            throw new ParseException("Quotes �" + quote + "� expected.", ctx.idx());
        }
        nextChar();

        StringBuilder sb = new StringBuilder();
        while (ctx.isNotEmpty()) {
            char c = ctx.get();
            if (c == quote) {
                nextChar();
                if (ctx.isNotEmpty() && ctx.get() == quote) {
                    sb.append(quote);
                    nextChar();
                } else {
                    break;
                }
            } else {
                sb.append(c);
                nextChar();
            }
        }
        nextRecord.add(sb.toString());
    }

    private void parseFieldWithoutQuotes() {
        StringBuilder sb = new StringBuilder();

        while (ctx.isNotEmpty()) {
            char c = ctx.get();
            if (c == separator || checkRecordSeparator()) {
                break;
            }
            sb.append(c);
            nextChar();
        }

        nextRecord.add(sb.toString());
    }

    private boolean parseSeparators() {
        boolean fieldSep = ctx.isNotEmpty() && ctx.get() == separator;
        if (fieldSep) {
            nextChar();
        }
        return fieldSep;
    }

    private void parseRecordSeparator() {
        if (ctx.isNotEmpty()) {
            switch (ctx.get()) {
                case LF:
                    nextChar();
                    break;

                case CR:
                    nextChar();
                    if (ctx.isEmpty() || ctx.get() != LF) {
                        throw new CsvException("End of line expected.", getPosition());
                    }
                    nextChar();
                    break;

                default:
                    throw new CsvException("End of line expected.", getPosition());
            }
        }
    }

    private boolean checkRecordSeparator() {
        if (ctx.isEmpty()) {
            return false;
        }
        switch (ctx.get()) {
            case LF:
                return true;
            case CR:
                return ctx.containts(1) && ctx.charAt(1) == LF;
            default:
                return false;
        }
    }
}

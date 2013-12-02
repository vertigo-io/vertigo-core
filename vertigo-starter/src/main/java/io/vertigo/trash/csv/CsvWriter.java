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
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;

import spark.commons.util.StringUtil;

/**
 * Classe de d'�criture d'un fichier CSV. Cette classe est l'�quivanlente en �criture de spark.commons.csv.CsvReader.
 */
public class CsvWriter {

    private static final char CR = '\r';
    private static final char LF = '\n';
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    private final char separator;
    private final char quote;

    private final Writer out;

    /** Indicateur de fermeture. */
    private boolean closed;

    /** Flag to keep open the underling output. */
    private final boolean keepOpen;

    /** CsvPosition du curseur. */
    private final CsvPosition position;

    /**
     * Construit un <tt>XMLWriter</tt> � partir d'un <tt>OutputStream</tt>.
     * 
     * @param out Flux de sortie.
     * @param header Indique s'il faut compter une ligne d'en-t�te.
     */
    public CsvWriter(Writer out, boolean header) {
        this(out, header, false, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    /**
     * Construit un <tt>XMLWriter</tt> � partir d'un <tt>OutputStream</tt>.
     * 
     * @param out Flux de sortie.
     * @param header Indique s'il faut compter une ligne d'en-t�te.
     * @param keepOpen Indique s'il faut fermer le flux soujacent.
     */
    public CsvWriter(Writer out, boolean header, boolean keepOpen) {
        this(out, header, keepOpen, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    /**
     * Construit un <tt>XMLWriter</tt> � partir d'un <tt>OutputStream</tt>.
     * 
     * @param out Flux de sortie.
     * @param header Indique s'il faut compter une ligne d'en-t�te.
     * @param separator Field separator.
     * @param quote Field quote.
     */
    public CsvWriter(Writer out, boolean header, char separator, char quote) {
        this(out, header, false, separator, quote);

    }

    /**
     * Construit un <tt>XMLWriter</tt> � partir d'un <tt>OutputStream</tt>.
     * 
     * @param out Flux de sortie.
     * @param header Indique s'il faut compter une ligne d'en-t�te.
     * @param keepOpen Indique s'il faut fermer le flux soujacent.
     * @param separator Field separator.
     * @param quote Field quote.
     */
    public CsvWriter(Writer out, boolean header, boolean keepOpen, char separator, char quote) {
        this.out = out;
        position = new CsvPosition(header ? 0 : 1);
        this.keepOpen = keepOpen;
        this.separator = separator;
        this.quote = quote;
    }

    /**
     * Lit la propri�t� <code>position</code>.
     * 
     * @return la valeur de <code>position</code>.
     */
    public CsvPosition getPosition() {
        return position;
    }

    /**
     * Ferme le Writer. Apr�s l'appel � <code>close()</code> il n'est plus possible de s�rialiser de nouvelle collection. Plusieurs appels
     * successifs de la m�thode ne provoquent pas d'erreur. Seul le premier appel provoque la fermeture.
     * 
     * @throws IOException Si une erreur se produit lors de la fermeture.
     */
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            if (keepOpen) {
                out.flush();
            } else {
                out.close();
            }
        }
    }

    /**
     * Ecrit une ligne.
     * 
     * @param record Listes des documents � imprimer.
     * @exception IllegalStateException quand l'<code>CsvWriter</code> n'a pas �t� initialis� o� s'il a �t� ferm�.
     * @throws IOException En cas d'erreur d'entr�-sortie.
     */
    public void write(String... record) throws IllegalStateException, IOException {
        write(Arrays.asList(record));
    }

    /**
     * Ecrit une ligne.
     * 
     * @param record Listes des documents � imprimer.
     * @exception IllegalStateException quand l'<code>CsvWriter</code> n'a pas �t� initialis� o� s'il a �t� ferm�.
     * @throws IOException En cas d'erreur d'entr�-sortie.
     */
    public void write(Iterable<String> record) throws IllegalStateException, IOException {
        if (closed) {
            throw new IllegalStateException("CsvWriter ferm�.");
        }

        Iterator<String> i = record.iterator();
        if (i.hasNext()) {
            writeField(i.next());
        }
        while (i.hasNext()) {
            out.write(separator);
            writeField(i.next());
        }

        out.write(CR);
        out.write(LF);
        position.addRecord();
    }

    private void writeField(String field) throws IOException {
        if (field != null && !field.isEmpty()) {
            out.write(encodeField(field));
        }
    }

    private String encodeField(String field) {
        int first = StringUtil.indexOf(field, quote, separator, CR, LF);
        if (first >= 0) {
            StringBuilder sb = new StringBuilder(field.length() * 2 + 2);
            sb.append(quote);
            if (first > 0) {
                sb.append(field.substring(0, first));
            }
            for (int i = first, n = field.length(); i < n; ++i) {
                char c = field.charAt(i);
                switch (c) {
                    case CR:
                        // Avoid to transform \r\n to double endl
                        if (i + 1 >= n || field.charAt(i + 1) != LF) {
                            sb.append(LF);
                        }
                        break;
                    case LF:
                        sb.append(LF);
                        break;

                    default:
                        if (c == quote) {
                            sb.append(quote).append(quote);
                        } else {
                            sb.append(c);
                        }
                        break;
                }
            }
            sb.append(quote);
            return sb.toString();
        } else {
            return field;
        }
    }
}

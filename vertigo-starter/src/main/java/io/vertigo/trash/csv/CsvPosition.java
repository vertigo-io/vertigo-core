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
/*
 * Created on 13 avr. 2004
 * by jmainaud
 */
package vertigo.trash.csv;

/**
 * Position in CSV File.
 */
public class CsvPosition {
    /** Record count. */
    private long record;

    /** Num�ro de la ligne du fichier. */
    private long line;

    /** Num�ro de la colonne du fichier (Nb caract�res). */
    private long column;

    /**
     * Construit une nouvelle instance de CsvPosition
     * 
     * @param record
     */
    CsvPosition(long record) {
        this(record, 0, 0);
    }

    /**
     * Create a new instance of CsvPosition.
     * 
     * @param line
     * @param column
     */
    private CsvPosition(long record, long line, long column) {
        this.record = record;
        this.line = line;
        this.column = column;
    }

    /**
     * Copie la position.
     * 
     * @return La copie de la position.
     */
    CsvPosition copy() {
        return new CsvPosition(record, line, column);
    }

    /**
     * Add a record.
     */
    void addRecord() {
        ++record;
    }

    /**
     * Add a line.
     */
    void addLine() {
        ++line;
        column = 0;
    }

    /**
     * Add a char.
     */
    void addChar() {
        ++column;
    }

    /**
     * @return �record� value.
     */
    public long getRecord() {
        return record;
    }

    /**
     * @return �line� value.
     */
    public long getLine() {
        return line;
    }

    /**
     * @return �column� value.
     */
    public long getColumn() {
        return column;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(line);
        sb.append(',');
        sb.append(column);
        sb.append(')');
        return sb.toString();
    }
}

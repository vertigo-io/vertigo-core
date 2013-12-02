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
 * @author jmainaud, $Author: pchretien $
 * @version $Revision: 1.1 $
 * @since 13 avr. 2004
 */
public class CsvException extends RuntimeException {
    /**
	 * 
	 */
    private static final long serialVersionUID = -5398124076836059798L;
    private final CsvPosition position;

    /**
     * Construit une nouvelle instance de CsvReaderException
     * 
     * @param position Position courante.
     */
    CsvException(String message, CsvPosition position) {
        super(message);
        this.position = position;
    }

    /**
     * Construit une nouvelle instance de CsvReaderException
     * 
     * @param position Position courante.
     * @param cause Cause de l'erreur.
     */
    CsvException(String message, CsvPosition position, Throwable cause) {
        super(message, cause);
        this.position = position;
    }

    /**
     * @return �position� value.
     */
    public CsvPosition getPosition() {
        return position;
    }
}

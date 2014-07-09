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
package io.vertigo.labs.impl.geocoder;

import io.vertigo.kernel.component.Plugin;
import io.vertigo.labs.geocoder.GeoLocation;

/**
 * @author spoitrenaud
 *
 */
public interface GeoCoderPlugin extends Plugin {
	/**
	 * Geocoding d'une adresse.
	 * 
	 * @param address Chaine de caract�res repr�sentant une adresse.
	 * @return Liste des emplacements (latitude ; longitude) correspondant � l'adresse recherch�e.
	 */
	GeoLocation findLocation(String address);
}

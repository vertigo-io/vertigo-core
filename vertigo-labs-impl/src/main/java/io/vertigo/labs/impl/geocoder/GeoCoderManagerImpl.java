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

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.labs.geocoder.GeoCoderManager;
import io.vertigo.labs.geocoder.GeoLocation;

import javax.inject.Inject;

/**
 * @author spoitrenaud
 *
 */
public final class GeoCoderManagerImpl implements GeoCoderManager {
	private final GeoCoderPlugin geoCoderPlugin;

	/**
	 * Constructeur.
	 * @param geoCoderPlugin Plugin de Geocoding
	 */
	@Inject
	public GeoCoderManagerImpl(final GeoCoderPlugin geoCoderPlugin) {
		Assertion.checkNotNull(geoCoderPlugin);
		//---------------------------------------------------------------------
		this.geoCoderPlugin = geoCoderPlugin;

	}

	/** {@inheritDoc} */
	public GeoLocation findLocation(final String address) {
		return geoCoderPlugin.findLocation(address);
	}

	/** {@inheritDoc} */
	public double distanceKm(final GeoLocation geoLocation1, final GeoLocation geoLocation2) {
		Assertion.checkArgument(!geoLocation1.isUndefined(), "le premier point n'est pas défini");
		Assertion.checkArgument(!geoLocation2.isUndefined(), "le second point n'est pas défini");
		//-------------------------------------------------------------------------
		int R = 6371; // km
		final double theta = Math.toRadians(geoLocation2.getLongitude() - geoLocation1.getLongitude());
		double lat1 = Math.toRadians(geoLocation1.getLatitude());
		double lat2 = Math.toRadians(geoLocation2.getLatitude());
		return Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(theta)) * R;

	}

}

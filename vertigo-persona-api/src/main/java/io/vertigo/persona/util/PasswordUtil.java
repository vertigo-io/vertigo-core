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
package io.vertigo.persona.util;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;
import io.vertigo.kernel.lang.Assertion;

import java.util.Random;

/**
 * Classe utilitaire offrant un ensemble de services concernant les DtObject.
 * 
 * @author npiedeloup
 * @version $Id: PasswordUtil.java,v 1.6 2014/01/24 17:59:38 pchretien Exp $
 */
public final class PasswordUtil {
	private static final String SALT_USER_TYPED_CHAR = "esarintulomdpcfbvhgjqzyxwk0123456789,;:!?.ESARINTULOMDPCFBVHGJQZYXWK+&\"'(-_)=~#{[|\\^@]}^$*%�/�><*�?";
	private static final int SALT_SIZE = 64;

	/**
	 * Contructeur
	 */
	private PasswordUtil() {
		// Rien ici
	}

	/**
	 * Construit un sel idempotent � partir d'une graine fix�.
	 * @param seed Graine de g�n�ration (typiquement nom du projet)
	 * @return Sel pour les password
	 */
	public static String generateSalt(final CodecManager codecManager, final String seed) {
		Assertion.checkNotNull(codecManager);
		Assertion.checkArgNotEmpty(seed);
		//---------------------------------------------------------------------
		final Encoder<byte[], byte[]> md5Codec = codecManager.getMD5Encoder();
		final Encoder<byte[], String> hexCodec = codecManager.getHexEncoder();
		final long lSeed;
		try {
			final String seedMD5 = hexCodec.encode(md5Codec.encode(seed.getBytes("ISO-8859-1")));
			lSeed = Long.parseLong(seedMD5.substring(0, 15), 16);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Impossible de d�terminer le sel pour '" + seed + "'", e);
		}
		final Random random = new Random(lSeed);

		final StringBuilder sb = new StringBuilder(SALT_SIZE);
		int charIndex;
		for (int i = 0; i < SALT_SIZE; i++) {
			charIndex = randomLinearInteger(0, SALT_USER_TYPED_CHAR.length() - 1, 0.40, random);
			sb.append(SALT_USER_TYPED_CHAR.charAt(charIndex));
		}
		return sb.toString();
	}

	/**
	 * @param minValue Valeur minimum
	 * @param maxValue Valeur maximum
	 * @param pMin probabilit� de la valeur min (doit etre entre 0 et 1), on en d�duit pMax = 1 - pMin.
	 * @return Integer al�atoire entre min et max, avec une fonction de r�partition lin�aire.
	 */
	private static int randomLinearInteger(final int minValue, final int maxValue, final double pMin, final Random random) {
		Assertion.checkArgument(pMin >= 0 && pMin <= 1, "Les probabilit�s doivent etre entre 0 et 1");
		//---------------------------------------------------------------------
		double v = random.nextDouble() * 2;
		double x = random.nextDouble();
		while (v > (1 - 2 * pMin) * x + pMin) {
			v = random.nextDouble() * 2;
			x = random.nextDouble();
		}
		return (int) Math.floor(x * (maxValue - minValue) + minValue);
	}

}

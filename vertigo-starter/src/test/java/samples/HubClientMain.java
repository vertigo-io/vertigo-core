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
package samples;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.home.data.BioManager;
import io.vertigoimpl.engines.elastica.redis.RedisElasticaEngine;

import java.util.Scanner;


/**
 * 
 * On cr�e des taches et on lances simultan�ment des workers.
 * 
 * @author pchretien
 */

public final class HubClientMain extends AbstractMain {
	public static void main(final String[] args) throws Exception {
		System.out.println("Veuillez saisir le nombre d'appels client:");
		Scanner sc = new Scanner(System.in);
		String str = sc.nextLine();
		System.out.println("Vous avez saisi : " + str);
		sc.close();
		int count = Integer.valueOf(str);
		new HubClientMain().test(count);
	}

	void test(int count) {
		RedisElasticaEngine elasticaEngine = new RedisElasticaEngine();

		final ComponentSpaceConfig componentSpaceConfig = createConfig(elasticaEngine, false);
		Home.start(componentSpaceConfig);
		try {
			long start = System.currentTimeMillis();
			for (int i = 0; i < count; i++) {
				testClient(i == 0);
				if ((i + 1) % 1000 == 0) {
					System.out.println("  - Temps �coul� pour  [" + (i + 1) + "]: " + (System.currentTimeMillis() - start));
				}
			}
			System.out.println("XXXXXXXXXXXXXXXXXXXX");
			System.out.println("  Temps �coul� pour [" + count + "]: " + (System.currentTimeMillis() - start));
			System.out.println("XXXXXXXXXXXXXXXXXXXX");
		} finally {
			Home.stop();
		}
	}

	private void testClient(boolean display) {
		final int res = Home.getComponentSpace().resolve(BioManager.class).add(3, 5);
		if (display) {
			System.out.println("biores:>>" + res);
		}
	}
}

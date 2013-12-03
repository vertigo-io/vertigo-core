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
import io.vertigoimpl.engines.elastica.redis.RedisElasticaEngine;

import java.util.Scanner;


/**
 * 
 * On cr�e des taches et on lances simultan�ment des workers.
 * 
 * @author pchretien
 */

public final class HubServerMain extends AbstractMain {
	public static void main(final String[] args) throws Exception {
		new HubServerMain().test();
	}

	void test() {
		final RedisElasticaEngine elasticaEngine = new RedisElasticaEngine();
		final ComponentSpaceConfig componentSpaceConfig = createConfig(elasticaEngine, true);
		Home.start(componentSpaceConfig);
		elasticaEngine.start();
		try {
			System.out.println("Taper sur entr�e pour sortir");
			final Scanner sc = new Scanner(System.in);
			sc.nextLine();
			sc.close();
		} finally {
			elasticaEngine.stop();
			Home.stop();
		}
	}
}

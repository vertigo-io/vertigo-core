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
/*!! Exemples Topics */

/*!- D&eacute;claration de la classe */

package samples;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.home.data.BioManager;
import io.vertigo.kernel.home.data.BioManagerImpl;
import io.vertigo.kernel.home.data.MathManager;
import io.vertigo.kernel.home.data.MathManagerImpl;
import io.vertigo.kernel.home.data.MathPlugin;
import io.vertigoimpl.engines.rest.grizzly.GrizzlyRestEngine;

import org.junit.Assert;


public class SampleApp {

	public static void main(final String[] args) throws InterruptedException {
		new SampleApp().init();
	}

	void init() throws InterruptedException {
		/*!
		To boot *vertigo* **hdsjhdsj**
		# This is an H1

		## This is an H2

		###### This is an H6

		> This is a blockquote with two paragraphs. Lorem ipsum dolor sit amet,
		> consectetuer adipiscing elit. Aliquam hendrerit mi posuere lectus.
		> Vestibulum enim wisi, viverra nec, fringilla in, laoreet vitae, risus.
		> 
		> Donec sit amet nisl. Aliquam semper ipsum sit amet velit. Suspendisse
		> id sem consectetuer libero luctus adipiscing.

		*	Red
		*	Green
		*	Blue

		--test--
		you can use 
		    * java 
		    * or xml 
		    * or java and xml 
		    * or what you want, you justa have to writeyour own
		How to configure in java 
		*/
//		// @formatter:off
//		final HomeConfig config = new HomeConfigBuilder()
////		.wi
//		.withSilence(true)
//			.beginModule("spaces")
//				//.beginComponent(ResourceManager.class, ResourceManagerImpl.class)
//					
//				//.endComponent()
////				.beginComponent(SpacesManager.class, SpacesManagerImpl.class)
////					.withParam("port", "8083")
////				.endComponent()	
//			.endModule()	
//			.beginModule("spaces")
//			.endModule()	
//		.build();
//		// @formatter:on
		//		Home.start(config);
		//		try {
		//			Thread.sleep(100000);
		//		} finally {
		//			Home.stop();
		//		}

		//		@Test
		//		public void testHome() {
		// @formatter:off
			final ComponentSpaceConfig config = new ComponentSpaceConfigBuilder()
				.withRestEngine(new GrizzlyRestEngine(8086)) 
				.withParam("log4j.configurationFileName", "/log4j.xml")
				.withSilence(false)
				.beginModule("Bio")
					.beginComponent(BioManager.class, BioManagerImpl.class).endComponent()
					.beginComponent(MathManager.class, MathManagerImpl.class)
						.withParam("start", "100")
						.beginPlugin( MathPlugin.class)
							.withParam("factor", "20")
						.endPlugin()
					.endComponent()	
				.endModule()	
			.build();
			// @formatter:on

		Home.start(config);
		try {
			final BioManager bioManager = Home.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			Assert.assertEquals(366, res);
			Assert.assertTrue(bioManager.isActive());
			Thread.sleep(100000);
		} finally {
			Home.stop();
		}
	}

}

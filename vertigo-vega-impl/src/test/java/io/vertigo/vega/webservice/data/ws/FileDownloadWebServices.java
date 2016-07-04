/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.vega.webservice.data.ws;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;

//bas� sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/test")
public final class FileDownloadWebServices implements WebServices {

	@Inject
	private ResourceManager resourcetManager;
	@Inject
	private FileManager fileManager;

	@AnonymousAccessAllowed
	@GET("/downloadFileContentType")
	public VFile testDownloadFile(final @QueryParam("id") Integer id) {
		final URL imageUrl = resourcetManager.resolve("npi2loup.png");
		final File imageFile = asFile(imageUrl);
		return fileManager.createFile("image" + id + generateSpecialChars(id) + ".png", "image/png", imageFile);
	}

	private static String generateSpecialChars(final Integer id) {
		switch (id) {
			case 1:
				return "ÔÙæóñ";
			case 2:
				return "µ°«/";
			case 3:
				return "ÔÙæ óñµ°«/";
			case 4:
				return "€;_~";
			default:
				return "";
		}
	}

	private static File asFile(final URL url) {
		try {
			return new File(url.toURI());
		} catch (final URISyntaxException e) {
			return new File(url.getPath());
		}
	}
}

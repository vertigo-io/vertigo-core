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
package io.vertigo.vega.rest;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.vega.rest.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.rest.stereotype.GET;
import io.vertigo.vega.rest.stereotype.PathPrefix;
import io.vertigo.vega.rest.stereotype.QueryParam;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

//bas� sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/test")
public final class TesterFileDownload implements RestfulService {

	@Inject
	private ResourceManager resourcetManager;
	@Inject
	private FileManager fileManager;

	@AnonymousAccessAllowed
	@GET("/downloadFileContentType")
	public KFile testDownloadFile(final @QueryParam("id") Integer id) {
		final URL imageUrl = resourcetManager.resolve("npi2loup.png");
		final File imageFile = asFile(imageUrl);
		final KFile imageKFile = fileManager.createFile("image" + id + generateSpecialChars(id) + ".png", "image/png", imageFile);
		return imageKFile;
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
		File f;
		try {
			f = new File(url.toURI());
		} catch (final URISyntaxException e) {
			f = new File(url.getPath());
		}
		return f;
	}
}

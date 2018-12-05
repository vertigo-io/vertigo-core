/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.account.plugins.identityprovider.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.account.impl.identityprovider.IdentityProviderPlugin;
import io.vertigo.app.Home;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.impl.file.model.FSFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * A simple implementation of the Realm interface that
 * uses a set of configured user accounts and roles to support authentication and authorization.  Each account entry
 * specifies the username, password, and roles for a user.  Roles can also be mapped
 * to permissions and associated with users.
 * <p/>
 * User accounts and roles are stored in two {@code Map}s in memory, so it is expected that the total number of either
 * is not sufficiently large.
 *
 * @since 0.1
 */
public class TextIdentityProviderPlugin implements IdentityProviderPlugin, Activeable {
	private static final String PHOTO_URL_RESERVED_FIELD = "photoUrl";
	private final Pattern filePattern;
	private final List<String> filePatternFieldsOrdered;

	private final Map<String, IdentityUserInfo> users; //id-to-Account
	private final ResourceManager resourceManager;
	private final String filePath;
	private final String userDtDefinitionName;
	private final String userAuthTokenFieldName;

	/**
	 * Constructor.
	 * @param resourceManager Resource Manager
	 * @param filePath File path
	 * @param filePatternStr File Pattern (id, displayName, email, authToken, photoUrl)
	 * @param userAuthTokenFieldName Authent token field name
	 * @param userDtDefinitionName User dtDefinition name
	 */
	@Inject
	public TextIdentityProviderPlugin(
			@Named("filePath") final String filePath,
			@Named("filePattern") final String filePatternStr,
			@Named("userAuthTokenFieldName") final String userAuthTokenFieldName,
			@Named("userDtDefinitionName") final String userDtDefinitionName,
			final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		Assertion.checkArgNotEmpty(filePatternStr);
		Assertion.checkArgument(filePatternStr.contains("(?<"),
				"filePattern should be a regexp of named group for each User's entity fields plus reserved field '{0}' (like : '(?<id>\\S+);(?<name>\\S+);(?<email>\\S+);;(?<{0}>\\S+)' )", PHOTO_URL_RESERVED_FIELD);
		Assertion.checkArgument(filePatternStr.contains("(?<" + PHOTO_URL_RESERVED_FIELD + ">"),
				"filePattern should be a regexp of named group for each User's entity fields plus reserved field '{0}' (like : '(?<id>\\S+);(?<name>\\S+);(?<email>\\S+);;(?<{0}>\\S+)' )", PHOTO_URL_RESERVED_FIELD);
		Assertion.checkArgument(filePatternStr.contains("(?<" + userAuthTokenFieldName + ">"),
				"filePattern should contains the userAuthTokenFieldName : {0}", userAuthTokenFieldName);
		Assertion.checkArgNotEmpty(userDtDefinitionName);
		Assertion.checkArgNotEmpty(userAuthTokenFieldName);
		// -----
		this.resourceManager = resourceManager;
		this.filePath = filePath;
		//SimpleAccountRealms are memory-only realms
		users = new LinkedHashMap<>();
		filePattern = Pattern.compile(filePatternStr);
		filePatternFieldsOrdered = parsePatternFields(filePatternStr);
		this.userDtDefinitionName = userDtDefinitionName;
		this.userAuthTokenFieldName = userAuthTokenFieldName;
	}

	private static List<String> parsePatternFields(final String filePatternStr) {
		final List<String> fields = new ArrayList<>();
		final Pattern pattern = Pattern.compile("\\(?<(\\S)>.*\\)");
		final Matcher matcher = pattern.matcher(filePatternStr);
		while (matcher.find()) {
			fields.add(matcher.group(1));
		}
		return fields;
	}

	/** {@inheritDoc} */
	@Override
	public long getUsersCount() {
		return users.size();
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> List<E> getAllUsers() {
		return (List<E>) users.values().stream()
				.map(IdentityUserInfo::getUser)
				.collect(Collectors.toList());
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E getUserByAuthToken(final String userAuthToken) {
		return (E) users.get(userAuthToken).getUser();
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> Optional<VFile> getPhoto(final UID<E> accountURI) {
		final IdentityUserInfo identityAccountInfo = users.get(accountURI.getId());
		Assertion.checkNotNull(identityAccountInfo, "No account found for {0}", accountURI);
		if (identityAccountInfo.getPhotoUrl() == null) {
			return Optional.empty();
		}
		final Path photoFile = Paths.get(identityAccountInfo.getPhotoUrl());
		if (!Files.exists(photoFile)) {
			return Optional.empty();
		}
		try {
			final String contentType = Files.probeContentType(photoFile);
			return Optional.of(new FSFile("photoOf" + accountURI.getId(), contentType, photoFile));
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		final DtDefinition userDtDefinition = Home.getApp().getDefinitionSpace().resolve(userDtDefinitionName, DtDefinition.class);
		Assertion.checkState(userDtDefinition.contains(userAuthTokenFieldName), "User definition ({0}) should contains the userAuthTokenField ({1})", userDtDefinitionName, userAuthTokenFieldName);

		final URL realmURL = resourceManager.resolve(filePath);
		try {
			final String confTest = parseFile(realmURL);
			try (final Scanner scanner = new Scanner(confTest)) {
				while (scanner.hasNextLine()) {
					final String line = scanner.nextLine();
					parseUserInfo(line, userDtDefinition);
				}
			}
		} catch (final Exception e) {
			throw WrappedException.wrap(e, "Erreur durant la lecture du Realm " + realmURL);
		}
	}

	private void parseUserInfo(final String line, final DtDefinition userDtDefinition) throws FormatterException {
		final Matcher matcher = filePattern.matcher(line);
		String photoUrl = null;
		String userAuthToken = null;

		final Entity user = Entity.class.cast(DtObjectUtil.createDtObject(userDtDefinition));
		for (final String propertyName : filePatternFieldsOrdered) {
			final String valueStr = matcher.group(propertyName);
			if (PHOTO_URL_RESERVED_FIELD.equals(propertyName)) {
				photoUrl = valueStr;
			} else {
				setTypedValue(userDtDefinition, user, propertyName, valueStr);
				if (userAuthTokenFieldName.equals(propertyName)) {
					userAuthToken = valueStr;
				}
			}
		}
		Assertion.checkArgNotEmpty(userAuthToken, "User AuthToken not found");
		users.put(userAuthToken, new IdentityUserInfo(user, photoUrl));
	}

	private static void setTypedValue(final DtDefinition userDtDefinition, final Entity user, final String propertyName, final String valueStr) throws FormatterException {
		final DtField dtField = userDtDefinition.getField(propertyName);
		final Serializable typedValue = (Serializable) dtField.getDomain().stringToValue(valueStr);
		dtField.getDataAccessor().setValue(user, typedValue);
	}

	private static String parseFile(final URL url) throws IOException {
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			final StringBuilder buff = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				buff.append(line);
				line = reader.readLine();
				buff.append("\r\n");
			}
			return buff.toString();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		users.clear();
	}

}

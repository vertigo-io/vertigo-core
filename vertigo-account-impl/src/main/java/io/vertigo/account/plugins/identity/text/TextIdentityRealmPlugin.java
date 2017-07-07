/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.vertigo.account.plugins.identity.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.account.identity.Account;
import io.vertigo.account.impl.identity.IdentityRealmPlugin;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.domain.model.URI;
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
public class TextIdentityRealmPlugin implements IdentityRealmPlugin, Activeable {
	private final Pattern filePattern;

	private final Map<String, IdentityAccountInfo> users; //id-to-Account
	private final ResourceManager resourceManager;
	private final String filePath;

	/**
	 * Constructor.
	 * @param resourceManager Resource Manager
	 * @param filePath File path
	 * @param filePatternStr File Pattern (id, displayName, email, authToken, photoUrl)
	 */
	@Inject
	public TextIdentityRealmPlugin(@Named("filePath") final String filePath, @Named("filePattern") final String filePatternStr, final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		// -----
		this.resourceManager = resourceManager;
		this.filePath = filePath;
		//SimpleAccountRealms are memory-only realms
		users = new LinkedHashMap<>();
		filePattern = Pattern.compile(filePatternStr);

	}

	/** {@inheritDoc} */
	@Override
	public long getAccountsCount() {
		return users.size();
	}

	/** {@inheritDoc} */
	@Override
	public Collection<Account> getAllAccounts() {
		return users.values().stream()
				.map(IdentityAccountInfo::getAccount)
				.collect(Collectors.toList());
	}

	/** {@inheritDoc} */
	@Override
	public Account getAccountByAuthToken(final String userAuthToken) {
		return users.values().stream()
				.filter(accountInfo -> userAuthToken.equals(accountInfo.getAccount().getAuthToken()))
				.findFirst().get().getAccount();
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final URI<Account> accountURI) {
		final IdentityAccountInfo identityAccountInfo = users.get(accountURI.getId());
		Assertion.checkNotNull(identityAccountInfo, "No account found for {0}", accountURI);
		if (identityAccountInfo.getPhotoUrl() == null) {
			return Optional.empty();
		}
		final File photoFile = new File(identityAccountInfo.getPhotoUrl());
		if (!photoFile.exists()) {
			return Optional.empty();
		}
		try {
			final String contentType = Files.probeContentType(photoFile.toPath());
			return Optional.of(new FSFile("photoOf" + accountURI.getId(), contentType, photoFile));
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		final URL realmURL = resourceManager.resolve(filePath);
		try {
			final String confTest = parseFile(realmURL);
			try (final Scanner scanner = new Scanner(confTest)) {
				while (scanner.hasNextLine()) {
					final String line = scanner.nextLine();
					parseUserInfo(line);
				}
			}
		} catch (final Exception e) {
			throw WrappedException.wrap(e, "Erreur durant la lecture du Realm " + realmURL);
		}
	}

	private void parseUserInfo(final String line) {
		final Matcher matcher = filePattern.matcher(line);
		final String id = matcher.group(1);
		final String displayName = matcher.group(2);
		final String email = matcher.group(3);
		final String authToken = matcher.group(3);
		final String photoUrl = matcher.group(4);
		final Account account = Account.builder(id)
				.withDisplayName(displayName)
				.withEmail(email)
				.withAuthToken(authToken)
				.build();
		users.put(id, new IdentityAccountInfo(account, photoUrl));
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

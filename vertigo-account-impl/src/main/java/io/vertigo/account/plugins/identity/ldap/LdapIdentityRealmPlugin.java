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
package io.vertigo.account.plugins.identity.ldap;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

import io.vertigo.account.identity.Account;
import io.vertigo.account.identity.AccountGroup;
import io.vertigo.account.impl.identity.IdentityRealmPlugin;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.impl.file.model.StreamFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Source of identity.
 * @author npiedeloup
 */
public final class LdapIdentityRealmPlugin implements IdentityRealmPlugin {
	private static final String LDAP_PHOTO_MIME_TYPE = "image/jpeg";

	private static final Logger LOGGER = Logger.getLogger(LdapIdentityRealmPlugin.class);

	private static final String DEFAULT_CONTEXT_FACTORY_CLASS_NAME = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String SIMPLE_AUTHENTICATION_MECHANISM_NAME = "simple";
	private static final String DEFAULT_REFERRAL = "follow";

	private final CodecManager codecManager;
	private final String ldapServer;
	private final String ldapAccountBaseDn;
	private final String ldapGroupBaseDn;
	private final String ldapReaderLogin;
	private final String ldapReaderPassword;

	private final String ldapUserAuthAttribute;
	private final Map<AccountProperty, String> ldapAccountAttributeMapping; //Account properties to ldapAttribute
	private final Map<GroupProperty, String> ldapGroupAttributeMapping; //Group properties to ldapAttribute

	private enum AccountProperty {
		id, displayName, email, authToken, photo
	}

	private enum GroupProperty {
		id, displayName
	}

	/**
	 * Constructor.
	 * @param ldapServerHost Ldap Server host
	 * @param ldapServerPort Ldap server port (default : 389)
	 * @param ldapAccountBaseDn Base de recherche des DNs d'Accounts
	 * @param ldapGroupBaseDn Base de recherche des DNs de groups
	 * @param ldapReaderLogin Login du reader LDAP
	 * @param ldapReaderPassword Password du reader LDAP
	 * @param ldapUserAuthAttribute Ldap attribute use to find user by it's authToken
	 * @param ldapAccountAttributeMappingStr Mapping from LDAP to Account
	 * @param codecManager Codec Manager
	 */
	@Inject
	public LdapIdentityRealmPlugin(
			@Named("ldapServerHost") final String ldapServerHost,
			@Named("ldapServerPort") final String ldapServerPort,
			@Named("ldapAccountBaseDn") final String ldapAccountBaseDn,
			@Named("ldapGroupBaseDn") final String ldapGroupBaseDn,
			@Named("ldapReaderLogin") final String ldapReaderLogin,
			@Named("ldapReaderPassword") final String ldapReaderPassword,
			@Named("ldapUserAuthAttribute") final String ldapUserAuthAttribute,
			@Named("ldapAccountAttributeMapping") final String ldapAccountAttributeMappingStr,
			final CodecManager codecManager) {
		Assertion.checkArgNotEmpty(ldapServerHost);
		Assertion.checkArgNotEmpty(ldapServerPort);
		Assertion.checkArgNotEmpty(ldapAccountBaseDn);
		Assertion.checkNotNull(ldapGroupBaseDn);
		Assertion.checkArgNotEmpty(ldapReaderLogin);
		Assertion.checkNotNull(ldapReaderPassword);
		Assertion.checkArgNotEmpty(ldapUserAuthAttribute);
		Assertion.checkArgNotEmpty(ldapAccountAttributeMappingStr);
		Assertion.checkNotNull(codecManager);
		ldapServer = ldapServerHost + ":" + ldapServerPort;
		this.ldapAccountBaseDn = ldapAccountBaseDn;
		this.ldapGroupBaseDn = ldapGroupBaseDn;
		this.ldapReaderLogin = ldapReaderLogin;
		this.ldapReaderPassword = ldapReaderPassword;
		this.ldapUserAuthAttribute = ldapUserAuthAttribute;
		ldapAccountAttributeMapping = parseLdapAttributeMapping(ldapAccountAttributeMappingStr, AccountProperty.class);
		Assertion.checkArgNotEmpty(ldapAccountAttributeMapping.get(AccountProperty.id), "ldapAccountAttributeMapping must declare mapping for accountProperty {0}" + AccountProperty.id);
		Assertion.checkArgNotEmpty(ldapAccountAttributeMapping.get(AccountProperty.displayName), "ldapAccountAttributeMapping must declare mapping for accountProperty {0}" + AccountProperty.displayName);

		ldapGroupAttributeMapping = parseLdapAttributeMapping("distinguishedName:id, displayName:displayName", GroupProperty.class);
		this.codecManager = codecManager;
	}

	/** {@inheritDoc} */
	@Override
	public Account getAccountByAuthToken(final String userAuthToken) {
		LdapContext ldapContext = null;
		try {
			ldapContext = createLdapContext(ldapReaderLogin, ldapReaderPassword);
			return getAccountByAuthToken(userAuthToken, ldapContext);
		} finally {
			closeLdapContext(ldapContext);
		}
	}

	/** {@inheritDoc} */
	@Override
	public long getAccountsCount() {
		throw new UnsupportedOperationException("Can't count all account from LDAP : anti-spooffing protections");
	}

	/** {@inheritDoc} */
	@Override
	public Collection<Account> getAllAccounts() {
		LdapContext ldapContext = null;
		try {
			ldapContext = createLdapContext(ldapReaderLogin, ldapReaderPassword);
			return searchAccount("*", -1, ldapContext);
		} finally {
			closeLdapContext(ldapContext);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final URI<Account> accountURI) {
		LdapContext ldapContext = null;
		try {
			ldapContext = createLdapContext(ldapReaderLogin, ldapReaderPassword);
			final String photoAttributeName = ldapAccountAttributeMapping.get(AccountProperty.photo);
			return parseOptionalVFile(getAccountAttributes(accountURI.getId(), Collections.singleton(photoAttributeName), ldapContext));
		} finally {
			closeLdapContext(ldapContext);
		}
	}

	private static <E extends Enum<E>> Map<E, String> parseLdapAttributeMapping(final String ldapAttributeMapping, final Class<E> enumClass) {
		final Map<E, String> accountAttributeMapping = new HashMap<>();
		for (final String mapping : ldapAttributeMapping.split("\\s*,\\s*")) {
			final String[] splitedMapping = mapping.split("\\s*:\\s*");
			Assertion.checkArgument(splitedMapping.length == 2, "Mapping should respect the pattern : LdapAttr1:AccountAttr1, LdapAttr2:AccountAttr2, ... (check : {0})", ldapAttributeMapping);
			//It's reverse compared to config String : we keep a map of key:accountProperty -> value:ldapAttribute
			accountAttributeMapping.put(Enum.valueOf(enumClass, splitedMapping[1]), splitedMapping[2]);
		}
		return accountAttributeMapping;
	}

	private Account getAccountByAuthToken(final String authToken, final LdapContext ctx) {
		final List<Attributes> result = searchLdapAttributes(ldapAccountBaseDn, "(" + ldapUserAuthAttribute + "=" + protectLdap(authToken) + "))", 2, ldapAccountAttributeMapping.values(), ctx);
		Assertion.checkState(!result.isEmpty(), "Can't found any user with authToken : {0}", ldapUserAuthAttribute);
		Assertion.checkState(result.size() == 1, "Too many user with same authToken ({0} shoud be unique)", ldapUserAuthAttribute);
		return parseAccount(result.get(0));
	}

	private Collection<Account> searchAccount(final String searchRequest, final int top, final LdapContext ldapContext) {
		final List<Attributes> result = searchLdapAttributes(ldapAccountBaseDn, searchRequest, top, ldapAccountAttributeMapping.values(), ldapContext);
		return result.stream()
				.map(this::parseAccount)
				.collect(Collectors.toList());
	}

	private Collection<AccountGroup> searchGroup(final String searchRequest, final int top, final LdapContext ldapContext) {
		final List<Attributes> result = searchLdapAttributes(ldapGroupBaseDn, searchRequest, top, ldapGroupAttributeMapping.values(), ldapContext);
		return result.stream()
				.map(this::parseGroup)
				.collect(Collectors.toList());
	}

	private Attributes getAccountAttributes(final Serializable accountId, final Set<String> returningAttributes, final LdapContext ldapContext) {
		final String ldapIdAttr = ldapAccountAttributeMapping.get(AccountProperty.id);
		final List<Attributes> result = searchLdapAttributes(ldapAccountBaseDn, "(" + ldapIdAttr + "=" + accountId + ")", 2, returningAttributes, ldapContext);
		Assertion.checkState(!result.isEmpty(), "Can't found any user with id : {0}", accountId);
		Assertion.checkState(result.size() == 1, "Too many user with same id ({0} shoud be unique)", accountId);
		return result.get(0);
	}

	private Attributes getGroupAttributes(final String groupDn, final Collection<String> returningGroupAttributes, final LdapContext ldapContext) {
		final List<Attributes> result = searchLdapAttributes(groupDn, "*", 2, returningGroupAttributes, ldapContext);
		Assertion.checkState(!result.isEmpty(), "Can't found any group with DN : {0}", groupDn);
		Assertion.checkState(result.size() == 1, "Too many group with same DN ({0} shoud be unique)", groupDn);
		return result.get(0);
	}

	private Optional<VFile> parseOptionalVFile(final Attributes attrs) {
		try {
			final String base64Content = parseOptionalAttribute(String.class, AccountProperty.photo, attrs);
			if (base64Content == null) {
				return Optional.empty();
			}
			final String displayName = parseAttribute(String.class, AccountProperty.displayName, attrs);
			return Optional.of(base64toVFile(displayName, base64Content));
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Can't parse Account from LDAP");
		}
	}

	private VFile base64toVFile(final String displayName, final String base64Content) {
		final byte[] photo = codecManager.getBase64Codec().decode(base64Content);
		return new StreamFile(displayName, LDAP_PHOTO_MIME_TYPE, new Date(), photo.length, () -> new ByteArrayInputStream(photo));
	}

	private LdapContext createLdapContext(final String principal, final String credentials) {
		final Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_CONTEXT_FACTORY_CLASS_NAME);
		env.put(Context.REFERRAL, DEFAULT_REFERRAL);

		env.put(Context.SECURITY_AUTHENTICATION, SIMPLE_AUTHENTICATION_MECHANISM_NAME);
		final String url = "ldap://" + ldapServer;
		env.put(Context.PROVIDER_URL, url);
		if (credentials != null) {
			env.put(Context.SECURITY_PRINCIPAL, principal);
			env.put(Context.SECURITY_CREDENTIALS, credentials);
		} else {
			env.put(Context.SECURITY_AUTHENTICATION, "none");
		}
		try {
			return new InitialLdapContext(env, null);
		} catch (final CommunicationException e) {
			throw WrappedException.wrap(e, "Can't connect to LDAP : {0} ", ldapServer);
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Can't connect user : {0} ", principal);
		}
	}

	private static String protectLdap(final String principal) {
		return EsapiLdapEncoder.encodeForDN(principal);
	}

	private static void closeLdapContext(final LdapContext ldapContext) {
		if (ldapContext != null) {
			try {
				ldapContext.close();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Fermeture connexion Ldap  \"" + ldapContext.toString() + "\" ");
				}
			} catch (final NamingException e) {
				throw WrappedException.wrap(e, "Erreur lors de la fermeture de l'objet LdapContext");
			}
		}
	}

	private Account parseAccount(final Attributes attrs) {
		try {
			final String accountId = parseAttribute(String.class, AccountProperty.id, attrs);
			final String authToken = parseAttribute(String.class, AccountProperty.authToken, attrs);
			final String displayName = parseAttribute(String.class, AccountProperty.displayName, attrs);
			final String email = parseOptionalAttribute(String.class, AccountProperty.email, attrs);
			return Account.builder(accountId)
					.withAuthToken(authToken)
					.withDisplayName(displayName)
					.withEmail(email)
					.build();
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Can't parse Account from LDAP");
		}
	}

	private AccountGroup parseGroup(final Attributes attrs) {
		try {
			final String groupId = String.class.cast(attrs.get(ldapGroupAttributeMapping.get(GroupProperty.id)).get());
			final String displayName = String.class.cast(attrs.get(ldapGroupAttributeMapping.get(GroupProperty.displayName)).get());
			return new AccountGroup(groupId, displayName);
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Can't parse Group from LDAP");
		}
	}

	private <O> O parseAttribute(final Class<O> valueClass, final AccountProperty accountProperty, final Attributes attrs) throws NamingException {
		return valueClass.cast(attrs.get(ldapAccountAttributeMapping.get(accountProperty)).get());
	}

	private <O> O parseOptionalAttribute(final Class<O> valueClass, final AccountProperty accountProperty, final Attributes attrs) {
		final String emailAttributeName = ldapAccountAttributeMapping.get(accountProperty);
		if (emailAttributeName != null) {
			final Attribute emailAttribute = attrs.get(emailAttributeName);
			if (emailAttribute != null) {
				try {
					return valueClass.cast(emailAttribute.get());
				} catch (final NamingException e) {
					throw WrappedException.wrap(e, "Ldap attribute {0} found, but is empty", emailAttributeName);
				}
			}
		}
		return null;
	}

	private List<Attributes> searchLdapAttributes(final String ldapBaseDn, final String searchRequest, final int top, final Collection<String> returningAttributes, final LdapContext ctx) {
		final List<Attributes> userAttributes = new ArrayList<>();
		final SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		constraints.setReturningAttributes(returningAttributes.toArray(new String[returningAttributes.size()]));
		try {
			int count = 0;
			final NamingEnumeration<SearchResult> answer = ctx.search(ldapBaseDn, searchRequest, constraints);
			while (answer.hasMore() && (top == -1 || count++ < top)) {
				final Attributes attrs = answer.next().getAttributes();
				userAttributes.add(attrs);
			}
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Can't search LDAP user with request: {0}", searchRequest);
		}
		return userAttributes;
	}

}

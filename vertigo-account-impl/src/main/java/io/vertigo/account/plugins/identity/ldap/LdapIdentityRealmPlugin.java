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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import io.vertigo.account.impl.identity.IdentityRealmPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Source of identity.
 * @author npiedeloup
 */
public final class LdapIdentityRealmPlugin implements IdentityRealmPlugin {
	private static final Logger LOGGER = Logger.getLogger(LdapIdentityRealmPlugin.class);

	private static final String DEFAULT_CONTEXT_FACTORY_CLASS_NAME = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String SIMPLE_AUTHENTICATION_MECHANISM_NAME = "simple";
	private static final String DEFAULT_REFERRAL = "follow";

	private static final String USERLOGIN_SUBSTITUTION_TOKEN = "{0}";
	private final String ldapServer;
	private final String ldapBaseDn;
	private final String ldapReaderLogin;
	private final String ldapReaderPassword;

	private final String ldapUserAuthAttribute;
	private final Map<String, String> ldapAccountAttributeMapping; //Account properties to ldapAttribute

	private enum AccountProperty {
		id, displayName, email, photo
	}

	/**
	 * Constructor.
	 * @param ldapServerHost Ldap Server host
	 * @param ldapServerPort Ldap server port (default : 389)
	 * @param ldapBaseDn Base de recherche des DNs
	 * @param ldapReaderLogin Login du reader LDAP
	 * @param ldapReaderPassword Password du reader LDAP
	 * @param ldapUserAuthAttribute Ldap attribute use to find user by it's authToken
	 * @param ldapAccountAttributeMapping Mapping from LDAP to Account
	 */
	@Inject
	public LdapIdentityRealmPlugin(@Named("ldapServerHost") final String ldapServerHost,
			@Named("ldapServerPort") final String ldapServerPort,
			@Named("ldapBaseDn") final String ldapBaseDn,
			@Named("ldapReaderLogin") final String ldapReaderLogin,
			@Named("ldapReaderPassword") final String ldapReaderPassword,
			@Named("ldapUserAuthAttribute") final String ldapUserAuthAttribute,
			@Named("ldapAccountAttributeMapping") final String ldapAccountAttributeMapping) {
		ldapServer = ldapServerHost + ":" + ldapServerPort;
		this.ldapBaseDn = ldapBaseDn;
		this.ldapReaderLogin = ldapReaderLogin;
		this.ldapReaderPassword = ldapReaderPassword;
		this.ldapUserAuthAttribute = ldapUserAuthAttribute;
		this.ldapAccountAttributeMapping = parseAccountAttributeMapping(ldapAccountAttributeMapping);
	}

	private static Map<String, String> parseAccountAttributeMapping(final String ldapAccountAttributeMapping) {
		final Map<String, String> accountAttributeMapping = new HashMap<>();
		for (final String mapping : ldapAccountAttributeMapping.split("\\s*,\\s*")) {
			final String[] splitedMapping = mapping.split("\\s*:\\s*");
			Assertion.checkArgument(splitedMapping.length == 2, "Mapping should respect the pattern : LdapAttr1:AccountAttr1, LdapAttr2:AccountAttr2, ... (check : {0})", ldapAccountAttributeMapping);
			//It's reverse compared to config String : we keep a map of key:accountProperty -> value:ldapAttribute
			accountAttributeMapping.put(splitedMapping[1], splitedMapping[2]);
		}
		Assertion.checkArgNotEmpty(accountAttributeMapping.get(AccountProperty.id), "ldapAccountAttributeMapping must declare mapping for accountProperty {0}" + AccountProperty.id);
		Assertion.checkArgNotEmpty(accountAttributeMapping.get(AccountProperty.displayName), "ldapAccountAttributeMapping must declare mapping for accountProperty {0}" + AccountProperty.displayName);
		return accountAttributeMapping;
	}

	private Account getUserBasicAttributes(final String userProtectedDn, final LdapContext ctx) {
		Account user = null;
		final SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		final Set<String> returningAttributes = ldapAccountAttributeMapping.keySet();
		constraints.setReturningAttributes(returningAttributes.toArray(new String[returningAttributes.size()]));
		try {
			final NamingEnumeration<SearchResult> answer = ctx.search(ldapBaseDn, "(" + ldapUserAuthAttribute + "=" + userProtectedDn + "))", constraints);
			if (answer.hasMore()) {
				final Attributes attrs = answer.next().getAttributes();
				user = parseAccount(attrs);
			}
			Assertion.checkState(!answer.hasMore(), "Too many user with same authToken ({0} shoud be unique)", ldapUserAuthAttribute);
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Can't read LDAP user {0}", userProtectedDn);
		}
		return user;
	}

	private Account parseAccount(final Attributes attrs) {
		try {
			final String accountId = String.class.cast(attrs.get(ldapAccountAttributeMapping.get(AccountProperty.id)).get());
			final String displayName = String.class.cast(attrs.get(ldapAccountAttributeMapping.get(AccountProperty.displayName)).get());

			String email = null;
			final String emailAttributeName = ldapAccountAttributeMapping.get(AccountProperty.email);
			if (emailAttributeName != null) {
				final Attribute emailAttribute = attrs.get(ldapAccountAttributeMapping.get(AccountProperty.email));
				if (emailAttribute != null) {
					email = String.class.cast(emailAttribute.get());
				}
			}
			return Account.builder(accountId)
					.withDisplayName(displayName)
					.withEmail(email)
					.build();
		} catch (final NamingException e) {
			throw WrappedException.wrap(e, "Can't parse Account from LDAP");
		}
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

	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		LdapContext ldapContext = null;
		try {
			ldapContext = createLdapContext(ldapReaderLogin, ldapReaderPassword);
			final Account account = getUserBasicAttributes(userAuthToken, ldapContext);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Ouverture de connexion LDAP  \"" + ldapContext.toString() + "\"");
			}
			return Optional.ofNullable(account);
		} finally {
			closeLdapContext(ldapContext);
		}
	}

}

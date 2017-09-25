/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.account.plugins.account.cache.redis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import io.vertigo.account.identity.Account;
import io.vertigo.account.identity.AccountGroup;
import io.vertigo.account.impl.account.AccountCachePlugin;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.MapBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * @author pchretien
 */
public final class RedisAccountCachePlugin implements AccountCachePlugin {
	private static final String HPHOTO_BY_ACCOUNT_START_KEY = "photoByAccount:";
	private static final String HGROUP_START_KEY = "group:";
	private static final String HACCOUNT_START_KEY = "account:";
	private static final String HAUTHTOKEN_INDEX_KEY = "authToken";

	private static final String SGROUPS_BY_ACCOUNT_START_KEY = "groupsByAccount:";
	private static final String SACCOUNTS_BY_GROUP_START_KEY = "accountsByGroup:";

	private static final String SGROUPS_KEY = "groups";
	private static final String SACCOUNTS_KEY = "accounts";

	private final RedisConnector redisConnector;
	private final PhotoCodec photoCodec;

	/**
	 * @param redisConnector Connector Redis
	 * @param codecManager Codec manager
	 */
	@Inject
	public RedisAccountCachePlugin(final RedisConnector redisConnector, final CodecManager codecManager) {
		Assertion.checkNotNull(redisConnector);
		Assertion.checkNotNull(codecManager);
		//-----
		this.redisConnector = redisConnector;
		photoCodec = new PhotoCodec(codecManager);
	}

	/** {@inheritDoc} */
	@Override
	public void putAccount(final Account account) {
		Assertion.checkNotNull(account);
		//-----
		try (final Jedis jedis = redisConnector.getResource()) {
			try (final Transaction tx = jedis.multi()) {
				tx.hmset(HACCOUNT_START_KEY + account.getId(), account2Map(account));
				tx.hset(HAUTHTOKEN_INDEX_KEY, account.getAuthToken(), account.getId());
				tx.sadd(SACCOUNTS_KEY, account.getId());
				tx.exec();
			} catch (final IOException ex) {
				throw WrappedException.wrap(ex);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public long getAccountsCount() {
		try (final Jedis jedis = redisConnector.getResource()) {
			return jedis.scard(SACCOUNTS_KEY);
		}
	}

	/** {@inheritDoc} */
	@Override
	public long getGroupsCount() {
		try (final Jedis jedis = redisConnector.getResource()) {
			return jedis.scard(SGROUPS_KEY);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccount(final URI<Account> accountURI) {
		Assertion.checkNotNull(accountURI);
		//-----
		try (final Jedis jedis = redisConnector.getResource()) {
			final String key = HACCOUNT_START_KEY + accountURI.getId();
			if (jedis.exists(key)) {
				return Optional.of(map2Account(jedis.hgetAll(key)));
			}
			return Optional.empty();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void putGroup(final AccountGroup group) {
		Assertion.checkNotNull(group);
		//-----
		//----
		try (final Jedis jedis = redisConnector.getResource()) {
			try (final Transaction tx = jedis.multi()) {
				tx.hmset(HGROUP_START_KEY + group.getId(), group2Map(group));
				tx.sadd(SGROUPS_KEY, group.getId());
				tx.exec();
			} catch (final IOException ex) {
				throw WrappedException.wrap(ex);
			}

		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<AccountGroup> getGroup(final URI<AccountGroup> groupURI) {
		Assertion.checkNotNull(groupURI);
		//-----
		try (final Jedis jedis = redisConnector.getResource()) {
			final String key = HGROUP_START_KEY + groupURI.getId();
			if (jedis.exists(key)) {
				return Optional.of(map2Group(jedis.hgetAll(key)));
			}
			return Optional.empty();
		}
	}

	/*
	@Override
	public Collection<AccountGroup> getAllGroups() {
		final List<Response<Map<String, String>>> responses = new ArrayList<>();
		try (final Jedis jedis = redisConnector.getResource()) {
			final Set<String> ids = jedis.smembers(SACCOUNTS_KEY);
			try (final Transaction tx = jedis.multi()) {
				for (final String id : ids) {
					responses.add(tx.hgetAll(HACCOUNT_START_KEY + id));
				}
				tx.exec();
			} catch (final IOException ex) {
				throw WrappedException.wrap(ex);
			}
	
		}
		//----- we are using tx to avoid roundtrips
		final List<AccountGroup> groups = new ArrayList<>();
		for (final Response<Map<String, String>> response : responses) {
			final Map<String, String> data = response.get();
			if (!data.isEmpty()) {
				groups.add(map2Group(data));
			}
		}
		return groups;
	}*/

	/** {@inheritDoc} */
	@Override
	public void attach(final Set<URI<Account>> accountsURI, final URI<AccountGroup> groupURI) {
		Assertion.checkNotNull(accountsURI);
		Assertion.checkNotNull(groupURI);
		//-----
		try (final Jedis jedis = redisConnector.getResource()) {
			try (final Transaction tx = jedis.multi()) {
				for (final URI<Account> accountURI : accountsURI) {
					tx.sadd(SACCOUNTS_BY_GROUP_START_KEY + groupURI.getId(), accountURI.getId().toString());
					tx.sadd(SGROUPS_BY_ACCOUNT_START_KEY + accountURI.getId(), groupURI.getId().toString());
				}
				tx.exec();
			} catch (final IOException ex) {
				throw WrappedException.wrap(ex);
			}

		}
	}

	/** {@inheritDoc} */
	@Override
	public void attach(final URI<Account> accountURI, final Set<URI<AccountGroup>> groupURIs) {
		Assertion.checkNotNull(accountURI);
		Assertion.checkNotNull(groupURIs);
		//-----
		try (final Jedis jedis = redisConnector.getResource()) {
			try (final Transaction tx = jedis.multi()) {
				for (final URI<AccountGroup> groupURI : groupURIs) {
					tx.sadd(SACCOUNTS_BY_GROUP_START_KEY + groupURI.getId(), accountURI.getId().toString());
					tx.sadd(SGROUPS_BY_ACCOUNT_START_KEY + accountURI.getId(), groupURI.getId().toString());
				}
				tx.exec();
			} catch (final IOException ex) {
				throw WrappedException.wrap(ex);
			}

		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<URI<Account>> getAccountURIs(final URI<AccountGroup> groupURI) {
		Assertion.checkNotNull(groupURI);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Account.class);
		final Set<URI<Account>> set = new HashSet<>();
		try (final Jedis jedis = redisConnector.getResource()) {
			final Set<String> ids = jedis.smembers(SACCOUNTS_BY_GROUP_START_KEY + groupURI.getId());
			for (final String id : ids) {
				set.add(new URI<Account>(dtDefinition, id));
			}
			return set;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<URI<AccountGroup>> getGroupURIs(final URI<Account> accountURI) {
		Assertion.checkNotNull(accountURI);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(AccountGroup.class);
		final Set<URI<AccountGroup>> set = new HashSet<>();
		try (final Jedis jedis = redisConnector.getResource()) {
			final Set<String> ids = jedis.smembers(SGROUPS_BY_ACCOUNT_START_KEY + accountURI.getId());
			for (final String id : ids) {
				set.add(new URI<AccountGroup>(dtDefinition, id));
			}
			return set;
		}
	}

	private static Map<String, String> account2Map(final Account account) {
		return new MapBuilder<String, String>()
				.put("id", account.getId())
				.put("authToken", account.getAuthToken())
				.put("displayName", account.getDisplayName())
				.putNullable("email", account.getEmail())
				.build();
	}

	private static Account map2Account(final Map<String, String> data) {
		return Account.builder(data.get("id"))
				.withAuthToken(data.get("authToken"))
				.withDisplayName(data.get("displayName"))
				.withEmail(data.get("email"))
				.build();
	}

	private static Map<String, String> group2Map(final AccountGroup group) {
		return new MapBuilder<String, String>()
				.put("id", group.getId())
				.put("displayName", group.getDisplayName())
				.build();
	}

	private static AccountGroup map2Group(final Map<String, String> data) {
		return new AccountGroup(data.get("id"), data.get("displayName"));
	}

	/** {@inheritDoc} */
	@Override
	public void setPhoto(final URI<Account> accountURI, final VFile photo) {
		Assertion.checkNotNull(accountURI);
		Assertion.checkNotNull(photo);
		//-----
		final Map<String, String> vFileMapPhoto = photoCodec.vFile2Map(photo);
		try (final Jedis jedis = redisConnector.getResource()) {
			try (final Transaction tx = jedis.multi()) {
				tx.hmset(HPHOTO_BY_ACCOUNT_START_KEY + accountURI.getId(), vFileMapPhoto);
				tx.exec();
			} catch (final IOException ex) {
				throw WrappedException.wrap(ex);
			}

		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final URI<Account> accountURI) {
		final Map<String, String> result;
		try (final Jedis jedis = redisConnector.getResource()) {
			result = jedis.hgetAll(HPHOTO_BY_ACCOUNT_START_KEY + accountURI.getId());
		}
		if (result.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(photoCodec.map2vFile(result));
	}

	/** {@inheritDoc} */
	@Override
	public void reset() {
		try (final Jedis jedis = redisConnector.getResource()) {
			try (final Transaction tx = jedis.multi()) {
				//todo : les haccount, photos et accountsByGroup", "photoByAccount ne sont pas supprimées
				tx.del(SACCOUNTS_KEY, SGROUPS_KEY, "accountsByGroup", "photoByAccount", HAUTHTOKEN_INDEX_KEY);
				tx.exec();
			} catch (final IOException ex) {
				throw WrappedException.wrap(ex);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {
		try (final Jedis jedis = redisConnector.getResource()) {
			final String accountUri = jedis.hget(HAUTHTOKEN_INDEX_KEY, userAuthToken);
			if (accountUri != null) {
				return Optional.of(map2Account(jedis.hgetAll(HACCOUNT_START_KEY + accountUri)));
			}
			return Optional.empty();
		}
	}
}

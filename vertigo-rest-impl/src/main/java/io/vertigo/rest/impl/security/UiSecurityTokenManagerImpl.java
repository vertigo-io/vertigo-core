package io.vertigo.rest.impl.security;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.persona.security.UserSession;
import io.vertigo.rest.security.UiSecurityTokenManager;

import java.io.Serializable;
import java.util.UUID;

import javax.inject.Inject;

/**
 * Ui data security token.
 * Keep an security token by user and DtObject.
 *
 * @author npiedeloup
 */
public final class UiSecurityTokenManagerImpl implements UiSecurityTokenManager {

	private final KSecurityManager securityManager;
	//private final long timeoutSeconds;
	/** Object token, by */
	private final UiSecurityTokenCachePlugin uiSecurityTokenCachePlugin;

	//private final Map<String, Map<String, Object>> tokenMap = new HashMap<>();

	@Inject
	public UiSecurityTokenManagerImpl(final KSecurityManager securityManager, final UiSecurityTokenCachePlugin uiSecurityTokenCachePlugin) {
		Assertion.checkNotNull(securityManager);
		//Assertion.checkArgument(timeoutSeconds >= 1 && timeoutSeconds <= 172800, "Security token Timeout ({0}), should be set in seconds, positive and less than 2 days", timeoutSeconds);
		//---------------------------------------------------------------------
		this.securityManager = securityManager;
		this.uiSecurityTokenCachePlugin = uiSecurityTokenCachePlugin;
	}

	//---------------------------------------------------------------------------
	//------------------Gestion du rendu et des interactions---------------------
	//---------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public String put(final Serializable data) {
		Assertion.checkNotNull(data, "Data is mandatory");
		//---------------------------------------------------------------------
		final String objectUUID = UUID.randomUUID().toString();
		final String tokenKey = makeTokenKey(objectUUID);
		uiSecurityTokenCachePlugin.put(tokenKey, data);
		return objectUUID; //We only return the object part.
	}

	/** {@inheritDoc} */
	@Override
	public Serializable get(final String objectUUID) {
		Assertion.checkArgNotEmpty(objectUUID, "Security key is mandatory");
		//---------------------------------------------------------------------
		final String tokenKey = makeTokenKey(objectUUID);
		return uiSecurityTokenCachePlugin.get(tokenKey);
	}

	/** {@inheritDoc} */
	public Serializable getAndRemove(final String objectUUID) {
		Assertion.checkArgNotEmpty(objectUUID, "Security key is mandatory");
		//---------------------------------------------------------------------
		final String tokenKey = makeTokenKey(objectUUID);
		return uiSecurityTokenCachePlugin.getAndRemove(tokenKey);
	}

	private String makeTokenKey(final String objectUUID) {
		final Option<UserSession> userSessionOption = securityManager.getCurrentUserSession();
		Assertion.checkArgument(userSessionOption.isDefined(), "UserSession is mandatory for security token");
		//---------------------------------------------------------------------
		final StringBuilder sb = new StringBuilder(36 + 1 + 36);
		final String userUUID = userSessionOption.get().getSessionUUID().toString();
		sb.append(userUUID).append(":").append(objectUUID);
		final String tokenKey = sb.toString();
		return tokenKey;
	}
}

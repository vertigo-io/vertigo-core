package io.vertigo.struts2.impl.context;

import io.vertigo.core.lang.Assertion;
import io.vertigo.struts2.context.ContextCacheManager;
import io.vertigo.struts2.core.KActionContext;

import javax.inject.Inject;

/**
 * Manager de gestion du cache.
 *
 * @author pchretien
 */
public final class ContextCacheManagerImpl implements ContextCacheManager {
	private final ContextCachePlugin contextCachePlugin;

	/**
	 * Constructeur.
	 * @param contextCachePlugin Plugin de gestion du cache
	 */
	@Inject
	public ContextCacheManagerImpl(final ContextCachePlugin contextCachePlugin) {
		Assertion.checkNotNull(contextCachePlugin);
		//---------------------------------------------------------------------
		this.contextCachePlugin = contextCachePlugin;
	}

	//---------------------------------------------------------------------------
	//------------------Gestion du rendu et des interactions---------------------
	//---------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public void put(final KActionContext context) {
		contextCachePlugin.put(context);
	}

	/** {@inheritDoc} */
	@Override
	public KActionContext get(final String key) {
		return contextCachePlugin.get(key);
	}
}

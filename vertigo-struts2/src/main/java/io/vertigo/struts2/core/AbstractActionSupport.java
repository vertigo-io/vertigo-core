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
package io.vertigo.struts2.core;

import io.vertigo.app.Home;
import io.vertigo.core.component.di.injector.Injector;
import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.struts2.context.ContextCacheManager;
import io.vertigo.struts2.exception.ExpiredContextException;
import io.vertigo.struts2.exception.VSecurityException;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Enumeration;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletResponseAware;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Preparable;

/**
 * Super class des Actions struts.
 *
 * @author npiedeloup
 */
public abstract class AbstractActionSupport extends ActionSupport implements ModelDriven<KActionContext>, Preparable, ServletResponseAware {
	private static final long serialVersionUID = -1850868830308743394L;
	/** Clé de context du UiUtil. */
	public static final String UTIL_CONTEXT_KEY = "util";
	/** Clé de context du mode. */
	public static final String MODE_CONTEXT_KEY = "mode";
	//TODO voir pour déléguer cette gestion des modes
	/** Clé de context du mode Edit. */
	public static final String MODE_EDIT_CONTEXT_KEY = "modeEdit";
	/** Clé de context du mode ReadOnly. */
	public static final String MODE_READ_ONLY_CONTEXT_KEY = "modeReadOnly";
	/** Clé de context du mode Create. */
	public static final String MODE_CREATE_CONTEXT_KEY = "modeCreate";
	/** Préfix des clés des paramètres passés par l'url. */
	public static final String URL_PARAM_PREFIX = "params.";

	/**
	 * Indique que l'initialisation du context par un parametre de l'url est autorisé.
	 */
	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AcceptCtxQueryParam {
		//rien
	}

	private HttpServletResponse response;
	private KActionContext context;
	@Inject
	private ContextCacheManager contextCacheManager;
	@Inject
	private ParamManager paramManager;

	private final UiMessageStack uiMessageStack;

	/**
	 * Constructeur.
	 */
	protected AbstractActionSupport() {
		Injector.injectMembers(this, Home.getApp().getComponentSpace());
		uiMessageStack = new UiMessageStack(this);
	}

	/** {@inheritDoc} */
	@Override
	public final void prepare() throws ExpiredContextException, VSecurityException {
		final HttpServletRequest request = ServletActionContext.getRequest();
		prepareContext(request);
	}

	private void prepareContext(final HttpServletRequest request) throws ExpiredContextException, VSecurityException {
		final String ctxId = request.getParameter(KActionContext.CTX);
		if ("POST".equals(request.getMethod()) || ctxId != null && acceptCtxQueryParam()) {
			if (ctxId == null) {
				contextMiss(null);
			} else {
				context = contextCacheManager.get(ctxId);
				if (context == null) {
					contextMiss(ctxId);
				}
				context.makeModifiable();
			}
		} else {
			context = new KActionContext();
			initContextUrlParameters(request);
			//TODO vérifier que l'action demandée n'attendait pas de context : il va etre recrée vide ce qui n'est pas bon dans certains cas.
			preInitContext();
			Assertion.checkState(context.containsKey(UTIL_CONTEXT_KEY), "Pour surcharger preInitContext vous devez rappeler les parents super.preInitContext(). Action: {0}", getClass().getSimpleName());
			initContext();
		}
	}

	private boolean acceptCtxQueryParam() {
		return this.getClass().isAnnotationPresent(AcceptCtxQueryParam.class);
	}

	/**
	 * Appeler lorsque que le context est manquant.
	 * Par défaut lance une ExpiredContextException.
	 * Mais une action spécifique pourrait reconstruire le context si c'est pertinent.
	 * @param ctxId Id du context manquant (seule info disponible)
	 * @throws ExpiredContextException Context expiré (comportement standard)
	 */
	protected void contextMiss(final String ctxId) throws ExpiredContextException {
		throw new ExpiredContextException("Context ctxId:'" + ctxId + "' manquant");
	}

	/**
	 * Initialisation du context.
	 * Pour accepter initContext avec des paramètres de la request, il est possible de le faire avec ce code :
	 * <code>
	 * final RequestContainerWrapper container = new RequestContainerWrapper(ServletActionContext.getRequest());
	 * MethodUtil.invoke(this, "initContext", container);
	 * </code>
	 * @throws VSecurityException Si erreur de sécurité
	 */
	protected abstract void initContext() throws VSecurityException;

	/**
	 * Preinitialisation du context, pour ajouter les composants standard.
	 * Si surcharger doit rappeler le super.preInitContext();
	 */
	protected void preInitContext() {
		context.put("appVersion", paramManager.getStringValue("app.version"));
		context.put(UTIL_CONTEXT_KEY, new UiUtil());
		toModeReadOnly();
	}

	/**
	 * Initialisation du context pour ajouter les paramètres passés par l'url.
	 * Les paramètres sont préfixés par "param."
	 */
	private void initContextUrlParameters(final HttpServletRequest request) {
		String name;
		for (final Enumeration<String> names = request.getParameterNames(); names.hasMoreElements();) {
			name = names.nextElement();
			context.put(URL_PARAM_PREFIX + name, request.getParameterValues(name));
		}
	}

	/**
	 * Conserve et fige le context.
	 * Utilisé par le KActionContextStoreInterceptor.
	 */
	public final void storeContext() {
		context.makeUnmodifiable();
		contextCacheManager.put(context);
	}

	/** {@inheritDoc} */
	@GET
	@Override
	public String execute() {
		return NONE;
	}

	/** {@inheritDoc} */
	@Override
	public final void validate() {
		//rien
	}

	/** {@inheritDoc} */
	@Override
	public final KActionContext getModel() {
		return context;
	}

	/**
	 * Passe en mode edition.
	 */
	protected final void toModeEdit() {
		//TODO voir pour déléguer cette gestion des modes
		context.put(MODE_CONTEXT_KEY, FormMode.edit);
		context.put(MODE_READ_ONLY_CONTEXT_KEY, false);
		context.put(MODE_EDIT_CONTEXT_KEY, true);
		context.put(MODE_CREATE_CONTEXT_KEY, false);
	}

	/**
	 * Passe en mode creation.
	 */
	protected final void toModeCreate() {
		//TODO voir pour déléguer cette gestion des modes
		context.put(MODE_CONTEXT_KEY, FormMode.create);
		context.put(MODE_READ_ONLY_CONTEXT_KEY, false);
		context.put(MODE_EDIT_CONTEXT_KEY, false);
		context.put(MODE_CREATE_CONTEXT_KEY, true);
	}

	/**
	 * Passe en mode readonly.
	 */
	protected final void toModeReadOnly() {
		//TODO voir pour déléguer cette gestion des modes
		context.put(MODE_CONTEXT_KEY, FormMode.readOnly);
		context.put(MODE_READ_ONLY_CONTEXT_KEY, true);
		context.put(MODE_EDIT_CONTEXT_KEY, false);
		context.put(MODE_CREATE_CONTEXT_KEY, false);
	}

	/**
	 * @return Si on est en mode edition
	 */
	protected final boolean isModeEdit() {
		return context.get(MODE_CONTEXT_KEY) == FormMode.edit;
	}

	/**
	 * @return Si on est en mode readOnly
	 */
	protected final boolean isModeRead() {
		return context.get(MODE_CONTEXT_KEY) == FormMode.readOnly;
	}

	/**
	 * @return Si on est en mode create
	 */
	protected final boolean isModeCreate() {
		return context.get(MODE_CONTEXT_KEY) == FormMode.create;
	}

	/**
	 * @return AjaxResponseBuilder pour les requetes Ajax
	 */
	public final AjaxResponseBuilder createAjaxResponseBuilder() {
		//TODO Voir pour l'usage de return AjaxMessage ou FileMessage
		try {
			response.setCharacterEncoding("UTF-8");
			return new AjaxResponseBuilder(response.getWriter(), false);
		} catch (final IOException e) {
			throw new WrappedException("Impossible de récupérer la response.", e);
		}
	}

	/**
	 * @return VFileResponseBuilder pour l'envoi de fichier
	 */
	public final VFileResponseBuilder createVFileResponseBuilder() {
		return new VFileResponseBuilder(ServletActionContext.getRequest(), response);
	}

	/**
	 * @return Pile des messages utilisateur.
	 */
	public final UiMessageStack getUiMessageStack() {
		return uiMessageStack;
	}

	/** {@inheritDoc} */
	@Override
	public final void setServletResponse(final HttpServletResponse servletResponse) {
		response = servletResponse;
	}

	/** {@inheritDoc}
	 * @deprecated Utiliser getUiMessageStack() */
	//We keep the deprecated, to keep a warning to not use the struts method
	@Override
	@Deprecated
	public final void addActionMessage(final String message) {
		super.addActionMessage(message);
	}

	/** {@inheritDoc}
	 * @deprecated Utiliser getUiMessageStack() */
	//We keep the deprecated, to keep a warning to not use the struts method
	@Override
	@Deprecated
	public final void addActionError(final String message) {
		super.addActionError(message);
	}

	/** {@inheritDoc}
	 * @deprecated Utiliser getUiMessageStack() */
	//We keep the deprecated, to keep a warning to not use the struts method
	@Override
	@Deprecated
	public final void addFieldError(final String fieldName, final String errorMessage) {
		super.addFieldError(fieldName, errorMessage);
	}

}

package io.vertigo.struts2.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

/**
 * Utilitaire d'accès à la Request.
 * @author npiedeloup
 */
public final class UiRequestUtil {

	private UiRequestUtil() {
		//rien 
	}

	/**
	 * @return la requête HTTP dans un context Struts
	 * @Deprecated Ne pas utiliser : cas extrement rare (certificat CPs)
	 */
	@Deprecated
	public static HttpServletRequest getHttpServletRequest() {
		return ServletActionContext.getRequest();
	}

	/**
	 * @return la session HTTP dans un context Struts
	 * @Deprecated utiliser KSecurityManager
	 */
	@Deprecated
	public static HttpSession getHttpSession() {
		return ServletActionContext.getRequest().getSession(false);
	}

	/**
	 * Invalide la session Http (ie Logout)
	 */
	public static void invalidateHttpSession() {
		final HttpSession session = ServletActionContext.getRequest().getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	/**
	 * Retourne la valeur du paramètre de la requête HTTP à partir d'une clé.
	 * 
	 * @param key la clé du paramètre
	 * @return la valeur du paramètre
	 * @deprecated Utiliser le @Named du initContext, ou un ContextRef<Xxx> pour les autres cas
	 */
	@Deprecated
	public static String getRequestParameter(final String key) {
		return getHttpServletRequest().getParameter(key);
	}
}

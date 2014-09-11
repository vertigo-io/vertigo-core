package io.vertigo.struts2.impl.unknownhandler;

import io.vertigo.core.lang.Option;
import io.vertigo.core.util.StringUtil;
import io.vertigo.struts2.impl.MethodUtil;
import io.vertigo.struts2.impl.servlet.RequestContainerWrapper;

import java.lang.reflect.Method;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.UnknownHandler;
import com.opensymphony.xwork2.XWorkException;
import com.opensymphony.xwork2.config.entities.ActionConfig;

/**
 * Gestion du passage de paramètres aux Actions.
 * A ajouter dans le struts.xml :
 * <bean type="com.opensymphony.xwork2.UnknownHandler" name="handler" class="kasperimpl.ui.struts.unknownhandler.InjectParamsToActionMethodHandler"/>
 * 
 * Pour en d�clarer plusieurs rechercher "Stacking Unknown Handlers".
 * @see "http://struts.apache.org/release/2.3.x/docs/unknown-handlers.html"
 * 
 * @author npiedeloup
 */
public class InjectParamsToActionMethodHandler implements UnknownHandler {

	/** {@inheritDoc} */
	public ActionConfig handleUnknownAction(final String namespace, final String actionName) throws XWorkException {
		//Non pris en charge
		return null;
	}

	/** {@inheritDoc} */
	public Result handleUnknownResult(final ActionContext actionContext, final String actionName, final ActionConfig actionConfig, final String resultCode) throws XWorkException {
		//Non pris en charge
		return null;
	}

	/** {@inheritDoc} */
	public Object handleUnknownActionMethod(final Object action, final String methodName) throws NoSuchMethodException {
		Option<Method> actionMethod = MethodUtil.findMethodByName(action.getClass(), methodName);
		if (actionMethod.isEmpty()) {
			//method non trouvée, on test doXXX comme struts2 le fait de base
			final String prefixedMethodName = getPrefixedMethodName(methodName);
			actionMethod = MethodUtil.findMethodByName(action.getClass(), prefixedMethodName);
			if (actionMethod.isEmpty()) {
				//method non trouvée
				return null;
			}
		}
		final RequestContainerWrapper container = new RequestContainerWrapper(ServletActionContext.getRequest());
		return MethodUtil.invoke(action, actionMethod.get(), container);
	}

	private String getPrefixedMethodName(final String methodName) {
		return "do" + StringUtil.first2UpperCase(methodName);
	}
}

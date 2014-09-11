package io.vertigo.struts2.impl.interceptor;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;
import io.vertigo.struts2.impl.MethodUtil;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * Interceptor Struts limitant l'access direct aux Actions.
 * @author npiedeloup
 */
public class KActionRestrictAccessInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = -6847302589734386523L;

	/** {@inheritDoc} */
	@Override
	public String intercept(final ActionInvocation actionInvocation) throws Exception {
		final HttpServletRequest request = ServletActionContext.getRequest();
		final String methodName = actionInvocation.getProxy().getMethod();
		//si on est en GET, et que l'on appelle une action spécifique (autre que execute) 
		//on test la présence de l'annotation @GET
		if (request.getMethod().equals("GET") && !"execute".equals(methodName)) {
			final Option<Method> actionMethod = MethodUtil.findMethodByName(actionInvocation.getAction().getClass(), methodName);
			Assertion.checkArgument(actionMethod.isDefined(), "Method {0} not found in {1}", methodName, actionInvocation.getAction().getClass());
			if (!actionMethod.get().isAnnotationPresent(GET.class)) {
				throw new IllegalAccessException("Vous ne pouvez pas appeler " + actionInvocation.getAction().getClass().getSimpleName() + "." + methodName + " directement.");
			}
		}
		return actionInvocation.invoke();
	}
}

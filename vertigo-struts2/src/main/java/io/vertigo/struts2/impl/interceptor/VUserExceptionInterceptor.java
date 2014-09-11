package io.vertigo.struts2.impl.interceptor;

import io.vertigo.core.exception.VUserException;
import io.vertigo.struts2.core.AbstractActionSupport;
import io.vertigo.struts2.core.UiError;
import io.vertigo.struts2.core.ValidationUserException;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * Interceptor Struts des exceptions de type UserException pour ajouter les messages à la page et la r�afficher.
 * @author npiedeloup
 */
public class VUserExceptionInterceptor extends AbstractInterceptor {
	private static final long serialVersionUID = -3416159964166247585L;

	//private final ExceptionPreResultListener exceptionPreResultListener = new ExceptionPreResultListener();

	/** {@inheritDoc} */
	@Override
	public String intercept(final ActionInvocation actionInvocation) throws Exception {
		//actionInvocation.addPreResultListener(exceptionPreResultListener);
		try {
			return actionInvocation.invoke();
		} catch (final ValidationUserException e) {
			final AbstractActionSupport action = (AbstractActionSupport) actionInvocation.getAction();
			for (final UiError uiError : e.getUiErrors()) {
				if (uiError.getDtObject() != null) {
					action.getUiMessageStack().error(uiError.getErrorMessage().getDisplay(), uiError.getDtObject(), uiError.getFieldName());
				} else {
					action.getUiMessageStack().error(uiError.getErrorMessage().getDisplay());
				}
			}
			return Action.NONE;
		} catch (final VUserException e) {
			final ActionSupport action = (ActionSupport) actionInvocation.getAction();
			action.addActionError(e.getMessage());
			return Action.NONE;
		}
	}
	//	private class ExceptionPreResultListener implements PreResultListener {
	//		@Override
	//		public void beforeResult(final ActionInvocation invocation, final String resultCode) {
	//			// perform operation necessary before Result execution
	//		}
	//	}
}

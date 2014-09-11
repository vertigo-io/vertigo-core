package io.vertigo.struts2.impl.interceptor;

import io.vertigo.struts2.core.AbstractActionSupport;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.interceptor.PreResultListener;

/**
 * Interceptor Struts figeant le context.
 * @author npiedeloup
 */
public class KActionContextStoreInterceptor extends AbstractInterceptor {
	private static final long serialVersionUID = -3416159964166247585L;
	private final PreResultListener exceptionPreResultListener = new StoreContextPreResultListener();

	/** {@inheritDoc} */
	@Override
	public String intercept(final ActionInvocation actionInvocation) throws Exception {
		actionInvocation.addPreResultListener(exceptionPreResultListener);
		//try {
		return actionInvocation.invoke();
		//} finally {

		//}
	}

	private static class StoreContextPreResultListener implements PreResultListener {

		/** {@inheritDoc} */
		@Override
		public void beforeResult(final ActionInvocation actionInvocation, final String resultCode) {
			final AbstractActionSupport action = (AbstractActionSupport) actionInvocation.getAction();
			action.storeContext();
		}
	}
}

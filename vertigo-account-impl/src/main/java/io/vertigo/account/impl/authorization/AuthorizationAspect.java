package io.vertigo.account.impl.authorization;

import java.lang.reflect.Parameter;

import javax.inject.Inject;

import io.vertigo.account.authorization.AuthorizationManager;
import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.account.authorization.annotations.Secured;
import io.vertigo.account.authorization.annotations.SecuredOperation;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.aop.AspectMethodInvocation;
import io.vertigo.core.locale.MessageText;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.lang.Assertion;

/**
 * Aspect pour la gestion des Secured au niveau de la couche service.
 * @author npiedeloup
 */
public final class AuthorizationAspect implements Aspect {
	private final AuthorizationManager authorizationManager;

	/**
	 * Constructor
	 * @param authorizationManager the authorizationManager
	 */
	@Inject
	public AuthorizationAspect(final AuthorizationManager authorizationManager) {
		Assertion.checkNotNull(authorizationManager);
		//-----
		this.authorizationManager = authorizationManager;
	}

	@Override
	public Object invoke(final Object[] args, final AspectMethodInvocation methodInvocation) {
		final Secured secured = methodInvocation.getMethod().getAnnotation(Secured.class) != null
				? methodInvocation.getMethod().getDeclaringClass().getAnnotation(Secured.class)
				: methodInvocation.getMethod().getAnnotation(Secured.class);

		Assertion.checkNotNull(secured, "No Aspect if not @Secured (on {0})", methodInvocation.getMethod());
		if (!authorizationManager.hasAuthorization(secured::value)) {
			throw new VSecurityException(MessageText.of("Not enought authorizations"));//no too sharp info here : may use log
		}
		final Parameter[] parameters = methodInvocation.getMethod().getParameters();
		for (int i = 0; i < args.length; i++) {
			final Parameter parameter = parameters[i];
			final SecuredOperation securedOperation = parameter.getAnnotation(SecuredOperation.class);
			//On repère les paramètres qui ont le @SecuredOperation
			if (securedOperation != null) {
				//Ils doivent être de type KeyConcept (et même securedEntity mais il y aura une exception dans le isAuthorized)
				Assertion.checkArgument(args[i] instanceof KeyConcept, "Can't check authorization on arg{0} ({1})", i, args[i]);
				if (!authorizationManager.isAuthorized((KeyConcept) args[i], securedOperation::value)) {
					throw new VSecurityException(MessageText.of("Not enought authorizations"));//no too sharp info here : may use log
				}
			}
		}
		return methodInvocation.proceed(args);
	}

	@Override
	public Class<Secured> getAnnotationType() {
		return Secured.class;
	}
}

package io.vertigo.struts2.core;

import io.vertigo.core.Home;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.StringUtil;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.Formatter;

import java.io.Serializable;

import com.opensymphony.xwork2.ActionContext;

/**
 * Class utilitaire pour le rendu des pages en jsp/ftl.
 * @author npiedeloup
 */
public final class UiUtil implements Serializable {

	private static final long serialVersionUID = -5677843485950859547L;

	/**
	 * @param uiObject Object du context
	 * @return Nom de l'object dans le context
	 */
	public final String contextKey(final UiObject<?> uiObject) {
		final ActionContext actionContext = ActionContext.getContext();
		final KActionContext kActionContext = ((AbstractActionSupport) actionContext.getActionInvocation().getAction()).getModel();
		return kActionContext.findKey(uiObject);
	}

	/**
	 * @param fieldPath Chemin du champ
	 * @return Label du champs
	 */
	public final String label(final String fieldPath) {
		return getDtField(fieldPath).getLabel().getDisplay();
	}

	/**
	 * @param fieldPath Chemin du champ boolean
	 * @param value Valeur à formater
	 * @return rendu du champs boolean
	 */
	public final String formatBoolean(final String fieldPath, final Boolean value) {
		final Formatter formatter;
		if (!fieldPath.contains(".")) {
			formatter = Home.getDefinitionSpace().resolve("DO_OUI_NON", Domain.class).getFormatter();
		} else {
			formatter = getDtField(fieldPath).getDomain().getFormatter();
		}
		return formatter.valueToString(value, DataType.Boolean);
	}

	/**
	 * @param fieldPath Chemin du champ
	 * @return Si le champs est obligatoire
	 */
	public final boolean required(final String fieldPath) {
		if (fieldPath.indexOf('.') > 0) { //Le champs est n'est pas porté par un Object
			return getDtField(fieldPath).isNotNull();
		}
		return false; //on ne sait pas dire, mais on ne force pas à obligatoire
	}

	/**
	 * @param uiList liste du context
	 * @return Nom du champ display de cette liste
	 */
	public final String getDisplayField(final AbstractUiList<?> uiList) {
		final DtDefinition dtDefinition = uiList.getDtDefinition();
		return StringUtil.constToCamelCase(dtDefinition.getDisplayField().get().getName(), false);
	}

	/**
	 * @param uiList liste du context
	 * @return Nom du champ de la pk de cette liste
	 */
	public final String getIdField(final AbstractUiList<?> uiList) {
		final DtDefinition dtDefinition = uiList.getDtDefinition();
		return StringUtil.constToCamelCase(dtDefinition.getIdField().get().getName(), false);
	}

	private static DtField getDtField(final String fieldPath) {
		Assertion.checkArgument(fieldPath.indexOf('.') > 0, "Le champs n'est pas porté par un Object ({0})", fieldPath);
		//Assertion.checkArgument(fieldPath.indexOf('.') == fieldPath.lastIndexOf('.'), "Seul un point est autorisé ({0})", fieldPath);
		final String contextKey = fieldPath.substring(0, fieldPath.lastIndexOf('.'));
		final String fieldName = fieldPath.substring(fieldPath.lastIndexOf('.') + 1);
		final ActionContext actionContext = ActionContext.getContext();
		final Object contextObject = actionContext.getValueStack().findValue(contextKey);
		Assertion.checkNotNull(contextObject, "{0} n''est pas dans le context", contextKey);
		Assertion.checkArgument(contextObject instanceof UiObject || contextObject instanceof AbstractUiList, "{0}({1}) doit être un UiObject ou une UiList ", contextKey, contextObject.getClass().getSimpleName());

		final DtDefinition dtDefinition;
		if (contextObject instanceof UiObject) {
			dtDefinition = ((UiObject<?>) contextObject).getDtDefinition();
		} else {
			dtDefinition = ((AbstractUiList<?>) contextObject).getDtDefinition();
		}
		Assertion.checkNotNull(dtDefinition); //, "{0}({1}) doit être un UiObject ou un UiList ", contextKey, contextObject.getClass().getSimpleName());
		Assertion.checkNotNull(dtDefinition, "{0}({1}) doit être un UiObject ou un UiList ", contextKey, contextObject.getClass().getSimpleName());
		return dtDefinition.getField(StringUtil.camelToConstCase(fieldName));

	}
}

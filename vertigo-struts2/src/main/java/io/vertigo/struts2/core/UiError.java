package io.vertigo.struts2.core;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.MessageText;
import io.vertigo.core.util.StringUtil;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Message d'IHM.
 * @author npiedeloup
 */
public final class UiError {
	private final MessageText messageText;
	private final DtObject dtObject;
	private final DtField dtField;

	/**
	 * Constructeur.
	 * @param dtObject Message
	 * @param dtField Object portant le message
	 * @param messageText Champs portant le message
	 */
	UiError(final DtObject dtObject, final DtField dtField, final MessageText messageText) {
		Assertion.checkNotNull(dtObject);
		Assertion.checkNotNull(dtField);
		Assertion.checkNotNull(messageText);
		//-----------------------------------------------------------------
		this.dtObject = dtObject;
		this.dtField = dtField;
		this.messageText = messageText;
	}

	/**
	 * @return Objet porteur de l'erreur
	 */
	public DtObject getDtObject() {
		return dtObject;
	}

	/**
	 * @return Champ porteur de l'erreur
	 */
	public DtField getDtField() {
		return dtField;
	}

	/**
	 * @return Message d'erreur
	 */
	public MessageText getErrorMessage() {
		return messageText;
	}

	/**
	 * @return Nom du champ
	 */
	public String getFieldName() {
		return StringUtil.constToCamelCase(dtField.getName(), false);
	}
}

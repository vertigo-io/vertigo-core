package io.vertigo.rest.validation;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class d'enregistrement des messages. 
 * @author npiedeloup
 */
public final class UiMessageStack {

	private final List<String> globalErrorMessages = new ArrayList<>();
	private final List<String> globalWarningMessages = new ArrayList<>();
	private final List<String> globalInfoMessages = new ArrayList<>();

	private final Map<String, List<String>> fieldErrorMessages = new HashMap<>();
	private final Map<String, List<String>> fieldWarningMessages = new HashMap<>();
	private final Map<String, List<String>> fieldInfoMessages = new HashMap<>();

	/**
	 * Niveau du message.
	 * @author npiedeloup
	 */
	public static enum Level {
		/** Erreur. */
		ERROR,
		/** Warning. */
		WARNING,
		/** Info. */
		INFO;
	}

	/**
	 * Constructor.
	 */
	public UiMessageStack() {
		//nothing
	}

	/**
	 * Ajoute un message.
	 * @param level Niveau de message
	 * @param message Message
	 */
	public final void addGlobalMessage(final Level level, final String message) {
		switch (level) {
			case ERROR:
				globalErrorMessages.add(message);
				break;
			case WARNING:
				globalWarningMessages.add(message);
				break;
			case INFO:
				globalInfoMessages.add(message);
				break;
			default:
				throw new UnsupportedOperationException("Unknowned level");
		}
	}

	/**
	 * @param message Message d'erreur
	 */
	public final void error(final String message) {
		addGlobalMessage(Level.ERROR, message);
	}

	/**
	 * @param message Message d'alerte
	 */
	public final void warning(final String message) {
		addGlobalMessage(Level.WARNING, message);
	}

	/**
	 * @param message Message d'info
	 */
	public final void info(final String message) {
		addGlobalMessage(Level.INFO, message);
	}

	/**
	 * @param message Message d'erreur
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public final void error(final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(Level.ERROR, message, dto, fieldName);
	}

	/**
	 * @param message Message d'alerte
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public final void warning(final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(Level.WARNING, message, dto, fieldName);
	}

	/**
	 * @param message Message d'info
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public final void info(final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(Level.INFO, message, dto, fieldName);
	}

	public final void addFieldMessage(final Level level, final String message, final DtObject dto, final String fieldName) {
		addFieldMessage(level, message, DtObjectUtil.findDtDefinition(dto).getClassSimpleName(), fieldName);
	}

	public final void addFieldMessage(final Level level, final String message, final String contextKey, final String fieldName) {
		final Map<String, List<String>> fieldMessageMap;
		switch (level) {
			case ERROR:
				fieldMessageMap = fieldErrorMessages;
				break;
			case WARNING:
				fieldMessageMap = fieldWarningMessages;
				break;
			case INFO:
				fieldMessageMap = fieldInfoMessages;
				break;
			default:
				throw new UnsupportedOperationException("Unknowned level");
		}
		final String fieldKey = contextKey + "." + fieldName;
		List<String> messages = fieldMessageMap.get(fieldKey);
		if (messages == null) {
			messages = new ArrayList<>();
			fieldMessageMap.put(fieldKey, messages);
		}
		messages.add(message);
	}

	public boolean hasErrors() {
		return !globalErrorMessages.isEmpty() || !fieldErrorMessages.isEmpty();
	}

}

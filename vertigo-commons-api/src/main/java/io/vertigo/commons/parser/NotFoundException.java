package io.vertigo.commons.parser;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Exception levée lorsque la règle n'est pas respectée.
 * @author pchretien
 * @version $Id: NotFoundException.java,v 1.3 2013/08/22 14:52:32 npiedeloup Exp $
 */
public final class NotFoundException extends Exception {
	private static final long serialVersionUID = 6096295235950712319L;
	private final int index;
	private final String s;
	private final String comment;
	private final Object[] commentValues;

	/**
	 * Constructeur.
	 * @param s Texte parsé
	 * @param index Index dans le texte
	 * @param rootException Cause
	 * @param comment Explication de l'erreur au format MessageFormat si il y a des valeurs
	 * @param commentValues Valeurs pour le MessageFormat de comment (il n'est pas conseillé de les fusionner au préalable dans comment)
	 */
	public NotFoundException(final String s, final int index, final NotFoundException rootException, final String comment, final Object... commentValues) {
		super(rootException);
		this.index = index;
		this.s = s;
		this.comment = comment;
		this.commentValues = commentValues != null ? commentValues.clone() : null;
	}

	/**
	 * Ligne la plus avancée lors de de l'évaluation de la règle.
	 * Cette ligne indique l'endroit vraisemblable de l'erreur SI IL s'agit de la bonne règle !!
	 * @return Index probable de l'erreur.
	 */
	int getLine() {
		//On compte le nombre de lignes
		int line = 0;
		int next = 0;
		while (next != -1 && next < index) {
			line++;
			next = s.indexOf('\n', next + 1);
			if (next >= s.length()) {
				throw new IllegalStateException("Calcul du nombre de lignes erroné");
			}
		}
		return line;
	}

	/** {@inheritDoc} */
	@Override
	public String getMessage() {
		return displayPosition() + "\n\t" + displayRule();
	}

	/**
	 * @return Retourne le message complet de l'erreur avec la pile inversée des causes.
	 */
	public String getFullMessage() {
		final StringBuilder sb = new StringBuilder();
		sb.append(displayPosition());
		sb.append("\n");
		final List<String> errorRuleList = new ArrayList<>();
		Throwable cause = this;
		while (cause instanceof NotFoundException) {
			errorRuleList.add(0, ((NotFoundException) cause).displayRule());
			cause = cause.getCause();
		}
		String sep = "- ";
		int i = 1;
		for (final String msg : errorRuleList) {
			sb.append("\t");
			sb.append(i++);
			sb.append(sep);
			sb.append(msg);
			sb.append("\n");
			sep = "- dans ";
		}
		return sb.toString();
	}

	/**
	 * @return Chaîne de caractère pour la regle en question.
	 */
	private String displayRule() {
		final StringBuilder sb = new StringBuilder();
		sb.append("regle : ");
		if (commentValues != null && commentValues.length != 0) {
			sb.append(MessageFormat.format(comment, commentValues));
		} else {
			sb.append(comment);
		}
		return sb.toString();
	}

	/**
	 * @return Chaîne de caractère pour la position de l'erreur (ligne, colonne, extrait).
	 */
	private String displayPosition() {
		final StringBuilder sb = new StringBuilder();
		int start = index > 1 ? s.lastIndexOf('\n', index - 1) + 1 : 0;
		start = Math.max(index - 150, start);
		int end = s.indexOf('\n', index + 1);
		if (end == -1) {
			end = s.length();
		}
		end = Math.min(index + 150, end);
		final int pos = index - start;
		final StringBuilder pointeur = new StringBuilder(pos);
		for (int i = 0; i < pos; i++) {
			pointeur.append(' ');
		}
		pointeur.append("^^^");
		sb.append("Erreur à la ligne ");
		sb.append(getLine());
		sb.append(" colonne ");
		sb.append(index - start);
		sb.append("\n\tvers : ");
		sb.append(s.substring(start, end).replace('\t', ' '));
		sb.append("\n\t      ");
		sb.append(pointeur);
		return sb.toString();
	}

	/**
	 * @return Position du curseur au lancement de l'erreur
	 */
	public int getIndex() {
		return index;
	}
}

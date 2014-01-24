package io.vertigo.commons.script;

import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.kernel.component.Manager;

import java.util.List;

/** 
 * Gestion des manipulations sur des scripts.
 *
 * @author pchretien
 * @version $Id: ScriptManager.java,v 1.2 2013/10/22 12:31:36 pchretien Exp $
 */
public interface ScriptManager extends Manager {
	/**
	 * Parse le script, notifie le handler.
	 * La grammaire est constitu�es de simples balises (S�parateurs). 
	 *
	 * @param script Script � analyser
	 * @param scriptHandler Handler g�rant la grammaire analys�e
	 * @param separators Liste des s�parateurs autoris�s dans la grammaire.
	 */
	void parse(final String script, final ScriptParserHandler scriptHandler, final List<ScriptSeparator> separators);

	/**
	 * Evaluation du script.
	 * Transforme un script en text.
	 * @param script Script � �valuer
	 * @return Script �valu�
	 */
	String evaluateScript(final String script, final SeparatorType separatorType, final List<ExpressionParameter> parameters);

	/**
	 * Evaluation d'une expression et non d'un bloc de code.
	 *  Exemple d'expressions exprim�es en java 
	 *  - name  
	 *  - birthDate
	 *  - age>20
	 *  - salary>5000 && age <30
	 *  - name + surName
	 * @param expression Expression
	 * @param parameters Param�tres
	 * @param type Type retourn�
	 * @return R�sultat de l'expression apr�s �valuation
	 */
	<J> J evaluateExpression(final String expression, List<ExpressionParameter> parameters, Class<J> type);
}

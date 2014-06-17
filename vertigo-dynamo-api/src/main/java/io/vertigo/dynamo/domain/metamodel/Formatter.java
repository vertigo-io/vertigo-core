package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * Permet de définir des formats particuliers d'affichages et de saisie.
 *
 * La remontée des erreurs est asymétrique :
 * - stringToValue remonte une exception typée, qui est interceptée et présentée en erreur utilisateur
 *
 * @author pchretien
 * @version $Id: Formatter.java,v 1.2 2013/10/22 12:25:18 pchretien Exp $
 */
@Prefix("FMT")
public interface Formatter extends Definition {
	/**
	 * Nom de du formatter par défaut.
	 */
	String FMT_DEFAULT = "FMT_DEFAULT";

	/**
	 * Transforme une valeur typée en String.
	 * @param objValue Valeur typée
	 * @param dataType Type
	 * @return  chaine formattée
	 */
	String valueToString(Object objValue, DataType dataType);

	/**
	 * Transforme une String en valeur typée.
	 * @param strValue chaine saisie
	 * @param dataType Type
	 * @return  Valeur typée (déformattage)
	 * @throws FormatterException Erreur de parsing
	 */
	Object stringToValue(String strValue, DataType dataType) throws FormatterException;
}

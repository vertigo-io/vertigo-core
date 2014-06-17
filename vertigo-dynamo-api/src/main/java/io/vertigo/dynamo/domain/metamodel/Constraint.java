package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.kernel.lang.MessageText;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * Interface de base pour la définition de contrainte
 * s'appliquant à un champ d'une structure de données.
 * Lors de l'utilisation d'une structure de données dans un formulaire,
 * une vérification automatique des valeurs saisies est réalisée.
 * Le contrôle porte en particulier sur les contraintes définies pour un champ
 * via la notion de domaine.
 * En cas d'erreur, la méthode getErrorMessage retourne la description de l'erreur.
 * 
 * La déclaration des contraintes peut être réalisée :
 *  - dans le fichier ksp/xml
 *  - en java directement.
 *
 * @author plepaisant
 * @param <J> Type java de la propriété associée à la contrainte
 * @param <D> Type java de la valeur à contrôler
 */
@Prefix("CK")
public interface Constraint<J, D> extends Definition {
	/**
	 * Cette méthode permet de définir la propriété(le comportement générique) que la contrainte implémente.
	 *
	 * @return Propriété implémentée par la contrainte
	 */
	Property getProperty();

	/**
	 * Cette méthode permet de définir la valeur de la propriété(le comportement spécifique) que la contrainte implémente.
	 *
	 * @return Valeur de la propriété implémentée par la contrainte
	 */
	J getPropertyValue();

	/**
	 * Réalise la validation d'une valeur. 
	 * Dans le cas où la méthode renvoie false,
	 * l'appel de checkConstraint() sera suivi de l'appel de getErrorMessage().<br>
	 *
	 * @param value Valeur à évaluer.
	 * @return Résultat du test : true si value est admissible, false en cas d'erreur.
	 */
	boolean checkConstraint(D value);

	/**
	 * Retourne le message d'erreur concernant le dernier appel de la méthode
	 * checkConstraint() ayant renvoyé false. Le message doit commencer par un caractère
	 * en lettre minuscule et décrire l'action correctrice pour la contrainte
	 * n'ayant pas été respectée.
	 * 
	 * Exemple : "le champ doit être renseigné"
	 *
	 * @return Message d'erreur
	 */
	MessageText getErrorMessage();
}
